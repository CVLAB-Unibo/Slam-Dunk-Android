#include <jni.h>
#include <android/log.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include <Eigen/Geometry>
#include <g2o/core/hyper_graph_action.h>
#include <g2o/core/sparse_optimizer.h>
#include <g2o/core/base_binary_edge.h>
#include <g2o/types/slam3d/vertex_se3.h>
#include <g2o/types/slam3d/parameter_se3_offset.h>

#include <g2o/core/block_solver.h>
#include <g2o/core/optimization_algorithm_levenberg.h>
#include <g2o/solvers/pcg/linear_solver_pcg.h>

#include <g2o/core/hyper_dijkstra.h>

using namespace std;

#define  LOG_TAG	"G2O"
#define  LOGI(...)	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)	__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

/**
 * Stop iterating based on the gain which is (oldChi - currentChi) / currentChi.
 *
 * If the gain is larger than zero and below the threshold, then the optimizer is stopped.
 * Typically usage of this action includes adding it as a postIteration action, by calling
 * addPostIterationAction on a sparse optimizer.
 */
class ConvergenceCheckAction : public g2o::HyperGraphAction
{
	public:
		ConvergenceCheckAction(g2o::SparseOptimizer* opt, double error_th = 1e-8)
        : m_optimizer(opt), m_gain_th(error_th), m_last_chi2(0.), m_stop_flag(false)
		{}

		void reset(g2o::SparseOptimizer* opt = NULL, double error_th = -1.)
		{
			if(opt != NULL)
				m_optimizer = opt;
			if(error_th >= 0)
				m_gain_th = error_th;
			m_last_chi2 = 0.;
			m_stop_flag = false;
		}

		virtual g2o::HyperGraphAction* operator()(const g2o::HyperGraph* graph, g2o::HyperGraphAction::Parameters* = 0)
		{
			m_optimizer->computeActiveErrors();
			const double current_chi2 = m_optimizer->activeChi2();
			const double gain = m_last_chi2/current_chi2 - 1.0;
			if((gain >= 0 && gain < m_gain_th) || current_chi2 == 0.0)
			{
				// tell the optimizer to stop
				if(m_optimizer->forceStopFlag() != NULL)
					*(m_optimizer->forceStopFlag()) = true;
				else
				{
					m_stop_flag = true;
					m_optimizer->setForceStopFlag(&m_stop_flag);
				}
			}
			m_last_chi2 = current_chi2;
			return this;
		}

    private:
		g2o::SparseOptimizer* m_optimizer;
		double m_gain_th, m_last_chi2;
		bool m_stop_flag;
};

/** col-0 = point-0; col-1 = point-1 */
typedef Eigen::Matrix<double,3,2> XYZPairMeasurement;

class EdgeSE3XYZPair : public g2o::BaseBinaryEdge<3, XYZPairMeasurement, g2o::VertexSE3, g2o::VertexSE3>
{
	public:
		EIGEN_MAKE_ALIGNED_OPERATOR_NEW

		EdgeSE3XYZPair()
		: offset0_(NULL), offset1_(NULL), cache0_(NULL), cache1_(NULL), weight_(1)
		{
			jac_buffer_.setZero();
			resizeParameters(2);
			installParameter(offset0_, 0);
			installParameter(offset1_, 1);
		}

		inline void setWeight(double w)
		{
			weight_ = w;
		}

		inline void setPointPointMetric()
		{
			plane_plane_metric_ = false;
			_information.setIdentity();
		}

		inline void setPointPlaneMetric(const Eigen::Vector3d& normal0, double e = 1e-3)
		{
			plane_plane_metric_ = false;
			Eigen::Matrix3d info;
			info << e, 0, 0,
					0, e, 0,
					0, 0, 1;
			Eigen::Matrix3d rot0 = computeRotation(normal0);
			_information = rot0.transpose()*info*rot0;
		}

		inline void setPlanePlaneMetric(const Eigen::Vector3d& normal0, const Eigen::Vector3d& normal1, double e = 1e-3)
		{
			plane_plane_metric_ = true;
			Eigen::Matrix3d info;
			info << 1, 0, 0,
					0, 1, 0,
					0, 0, e;

			Eigen::Matrix3d rot = computeRotation(normal0);
			cov0_ = rot.transpose()*info*rot;
			rot = computeRotation(normal1);
			cov1_ = rot.transpose()*info*rot;
		}

		double weight() const
		{
			return weight_;
		}

		const Eigen::Matrix3d& cov0() const
		{
			return cov0_;
		}

		const Eigen::Matrix3d& cov1() const
		{
			return cov1_;
		}

		//TODO meglio spostare poi il tutto in un altro file!

		virtual bool read(std::istream& is)
		{
			int pid0, pid1;
			is >> pid0 >> pid1;
			if(!setParameterId(0, pid0))
				return false;
			if(!setParameterId(1, pid1))
				return false;
			if(!is.good())
				return false;

			is >> _measurement(0,0) >> _measurement(1,0) >> _measurement(2,0)
		       >> _measurement(0,1) >> _measurement(1,1) >> _measurement(2,1);
			if(!is.good())
				return false;

			for (unsigned i = 0; i < 3; ++i)
				for (unsigned j = 0; j < 3; ++j)
					is >> _information(i,j);
			if(!is.good())
				return false;

			int plpl;
			is >> weight_ >> plpl;
			plane_plane_metric_ = (plpl == 1 ? true : false);
			if(!is.good())
				return false;

			for (unsigned i = 0; i < 3; ++i)
				for (unsigned j = 0; j < 3; ++j)
					is >> cov0_(i,j);
			if(!is.good())
				return false;

			for (unsigned i = 0; i < 3; ++i)
				for (unsigned j = 0; j < 3; ++j)
					is >> cov1_(i,j);
			return true;
		}

		virtual bool write(std::ostream& os) const
		{
			os << offset0_->id() << " " << offset1_->id() << " ";
			os << _measurement(0,0) << " " << _measurement(1,0) << " " << _measurement(2,0) << " "
		       << _measurement(0,1) << " " << _measurement(1,1) << " " << _measurement(2,1) << " ";
			for (unsigned i = 0; i < 3; ++i)
				for (unsigned j = 0; j < 3; ++j)
					os << _information(i,j) << " ";
			os << weight_ << " " << (plane_plane_metric_ ? 1 : 0) << " ";
			for (unsigned i = 0; i < 3; ++i)
				for (unsigned j = 0; j < 3; ++j)
					os << cov0_(i,j) << " ";
			for (unsigned i = 0; i < 3; ++i)
				for (unsigned j = 0; j < 3; ++j)
					os << cov1_(i,j) << " ";

			return os.good();
		}

		virtual void computeError()
		{
			if(plane_plane_metric_)
			{
				const Eigen::Isometry3d n2n = cache0_->w2n() * cache1_->n2w();
				_error = weight_ * ((n2n * _measurement.col(1)) - _measurement.col(0));
				_information = (cov0_ + n2n.rotation() * cov1_ * n2n.rotation().transpose()).inverse();
			}
			else
				_error = weight_ * ((cache0_->w2n() * (cache1_->n2w() * _measurement.col(1))) - _measurement.col(0));
		}

		virtual bool setMeasurementData(const double* d)
		{
			_measurement = Eigen::Map<const XYZPairMeasurement>(d);
			return true;
		}

		virtual bool getMeasurementData(double* d) const
		{
			Eigen::Map<XYZPairMeasurement> v(d);
			v = _measurement;
			return true;
		}

		virtual void linearizeOplus()
		{
			const g2o::VertexSE3* vp0 = static_cast<const g2o::VertexSE3*>(_vertices[0]);
			const g2o::VertexSE3* vp1 = static_cast<const g2o::VertexSE3*>(_vertices[1]);

			if (!vp0->fixed())
			{
				const Eigen::Vector3d p1t = cache0_->w2l() *  (cache1_->n2w() * _measurement.col(1));
				jac_buffer_(1,0) = 2.0*p1t[2];
				jac_buffer_(2,0) = -2.0*p1t[1];
				jac_buffer_(0,1) = -2.0*p1t[2];
				jac_buffer_(2,1) = 2.0*p1t[0];
				jac_buffer_(0,2) = 2.0*p1t[1];
				jac_buffer_(1,2) = -2.0*p1t[0];

				_jacobianOplusXi.block<3,3>(0,0) = -1. * offset0_->inverseOffset().rotation();
				_jacobianOplusXi.block<3,3>(0,3) = offset0_->inverseOffset().rotation() * jac_buffer_;
			}

			if (!vp1->fixed())
			{
				const Eigen::Vector3d p1o = offset1_->offset() * _measurement.col(1);
				jac_buffer_(1,0) = -2.0*p1o[2];
				jac_buffer_(2,0) = 2.0*p1o[1];
				jac_buffer_(0,1) = 2.0*p1o[2];
				jac_buffer_(2,1) = -2.0*p1o[0];
				jac_buffer_(0,2) = -2.0*p1o[1];
				jac_buffer_(1,2) = 2.0*p1o[0];

				_jacobianOplusXj.block<3,3>(0,0) = cache0_->w2n().rotation() * vp1->estimate().rotation();
				_jacobianOplusXj.block<3,3>(0,3) = _jacobianOplusXj.block<3,3>(0,0) * jac_buffer_;
			}
		}

		virtual int measurementDimension() const
		{
			return 6;
		}

	private:
		inline Eigen::Matrix3d computeRotation(const Eigen::Vector3d& normal) const
		{
			Eigen::Matrix3d rot;
			rot.row(2) = normal;
			rot.row(1) = (Eigen::Vector3d::UnitY() - normal(1)*normal).normalized();
			rot.row(0) = normal.cross(rot.row(1));
			return rot;
		}

		virtual bool resolveCaches()
		{
			assert(offset0_ && offset1_);

			g2o::ParameterVector pv(2);
			pv[0] = offset0_;
			resolveCache(cache0_, (g2o::OptimizableGraph::Vertex*)_vertices[0], "CACHE_SE3_OFFSET", pv);
			pv[1] = offset1_;
			resolveCache(cache1_, (g2o::OptimizableGraph::Vertex*)_vertices[1], "CACHE_SE3_OFFSET", pv);
			return (cache0_ && cache1_);
		}

		g2o::ParameterSE3Offset *offset0_, *offset1_;
		g2o::CacheSE3Offset  *cache0_, *cache1_;

		double weight_;
		bool plane_plane_metric_;
		Eigen::Matrix3d cov0_, cov1_;
		Eigen::Matrix3d jac_buffer_;
};

extern "C"
{
	JNIEXPORT void JNICALL Java_it_unibo_slam_graph_GraphBackend_initNativeGraph(JNIEnv *env, jobject obj);
	JNIEXPORT jdouble JNICALL Java_it_unibo_slam_graph_GraphBackend_getChi2Native(JNIEnv *env, jobject obj);
	JNIEXPORT jdouble JNICALL Java_it_unibo_slam_graph_GraphBackend_getWeightedMeanChi2Native(JNIEnv *env, jobject obj);
	JNIEXPORT jint JNICALL Java_it_unibo_slam_graph_GraphBackend_getNumberOfEdgesNative(JNIEnv *env, jobject obj);
	JNIEXPORT void JNICALL Java_it_unibo_slam_graph_GraphBackend_clearNative(JNIEnv *env, jobject obj);
	JNIEXPORT void JNICALL Java_it_unibo_slam_graph_GraphBackend_setFixedViewNative(JNIEnv *env, jobject obj,
			jint viewId, jboolean fixed);
	JNIEXPORT void JNICALL Java_it_unibo_slam_graph_GraphBackend_addVertexWithEstimateNative(JNIEnv *env, jobject obj,
			jint poseId, jdoubleArray estimate);
	JNIEXPORT void JNICALL Java_it_unibo_slam_graph_GraphBackend_setVertexEstimateNative(JNIEnv *env, jobject obj,
			jint poseId, jdoubleArray estimate);
	JNIEXPORT jdoubleArray JNICALL Java_it_unibo_slam_graph_GraphBackend_getVertexEstimateNative(JNIEnv *env, jobject obj,
			jint poseId);
	JNIEXPORT void JNICALL Java_it_unibo_slam_graph_GraphBackend_removeVertexNative(JNIEnv *env, jobject obj,
			jint poseId);
	JNIEXPORT void JNICALL Java_it_unibo_slam_graph_GraphBackend_addEdgeNative(JNIEnv *env, jobject obj,
			jint referenceId, jint matchingId, jdoubleArray referenceVx, jdoubleArray matchingVx, jdouble score);
	JNIEXPORT jint JNICALL Java_it_unibo_slam_graph_GraphBackend_optimizeNative(JNIEnv *env, jobject obj,
			jint maximumNumberOfIterations);
	JNIEXPORT jint JNICALL Java_it_unibo_slam_graph_GraphBackend_relativeOptimizeNative(JNIEnv *env, jobject obj,
			jintArray vSet, jint size, jint maximumNumberOfIterations);
	JNIEXPORT void JNICALL Java_it_unibo_slam_graph_GraphBackend_createRBASubsetNative(JNIEnv *env, jobject obj,
			jint originId, jint ring, jobject vSet, jobject fSet);
}

// Global variables accessible only within this source file
namespace
{
	g2o::SparseOptimizer optimizer;
	ConvergenceCheckAction action = ConvergenceCheckAction(&optimizer, 1E-10);
	double edgeWeights = 0.0;
}

JNIEXPORT void JNICALL Java_it_unibo_slam_graph_GraphBackend_initNativeGraph(JNIEnv *env, jobject obj)
{
	action = ConvergenceCheckAction(&optimizer, 1E-10);
	edgeWeights = 0.0;

	typedef g2o::BlockSolver< g2o::BlockSolverTraits<6, 0> > BlockSolver_6_0;

	BlockSolver_6_0::LinearSolverType* linearSolver = new g2o::LinearSolverPCG<BlockSolver_6_0::PoseMatrixType>();
	BlockSolver_6_0* solverPointer = new BlockSolver_6_0(linearSolver);
	g2o::OptimizationAlgorithmLevenberg* optimizationAlgorithm = new g2o::OptimizationAlgorithmLevenberg(solverPointer);

	// Set the algorithm and the termination action
	optimizer.setAlgorithm(optimizationAlgorithm);
	optimizer.addPostIterationAction(&action);

	g2o::ParameterSE3Offset* offset = new g2o::ParameterSE3Offset();
	offset->setId(0);
	optimizer.addParameter(offset);
}

JNIEXPORT jdouble JNICALL Java_it_unibo_slam_graph_GraphBackend_getChi2Native(JNIEnv *env, jobject obj)
{
	return optimizer.activeChi2();
}

JNIEXPORT jdouble JNICALL Java_it_unibo_slam_graph_GraphBackend_getWeightedMeanChi2Native(JNIEnv *env, jobject obj)
{
	assert(edgeWeights > 0);
	return optimizer.activeChi2() / edgeWeights;
}

JNIEXPORT jint JNICALL Java_it_unibo_slam_graph_GraphBackend_getNumberOfEdgesNative(JNIEnv *env, jobject obj)
{
	return optimizer.edges().size();
}

JNIEXPORT void JNICALL Java_it_unibo_slam_graph_GraphBackend_clearNative(JNIEnv *env, jobject obj)
{
	optimizer.clear();
	edgeWeights = 0.0;
}

JNIEXPORT void JNICALL Java_it_unibo_slam_graph_GraphBackend_setFixedViewNative(JNIEnv *env, jobject obj,
		jint viewId, jboolean fixed)
{
	g2o::SparseOptimizer::Vertex* v = optimizer.vertex(viewId);
	if (v != NULL)
		v->setFixed(fixed);
}

JNIEXPORT void JNICALL Java_it_unibo_slam_graph_GraphBackend_addVertexWithEstimateNative(JNIEnv *env, jobject obj,
		jint poseId, jdoubleArray estimate)
{
	double tempEstimate[16];
	env->GetDoubleArrayRegion(estimate, 0, 16, tempEstimate);

	Eigen::Isometry3d estimateEigen(Eigen::Matrix4d::Map(tempEstimate));

	g2o::VertexSE3* vx = new g2o::VertexSE3();
	vx->setId(poseId);
	vx->setEstimate(estimateEigen);
	optimizer.addVertex(vx);
}

JNIEXPORT void JNICALL Java_it_unibo_slam_graph_GraphBackend_setVertexEstimateNative(JNIEnv *env, jobject obj,
		jint poseId, jdoubleArray estimate)
{
	double tempEstimate[16];
	env->GetDoubleArrayRegion(estimate, 0, 16, tempEstimate);

	Eigen::Isometry3d estimateEigen(Eigen::Matrix4d::Map(tempEstimate));

	static_cast<g2o::VertexSE3*>(optimizer.vertex(poseId))->setEstimate(estimateEigen);
}

JNIEXPORT jdoubleArray JNICALL Java_it_unibo_slam_graph_GraphBackend_getVertexEstimateNative(JNIEnv *env, jobject obj,
		jint poseId)
{
	jdoubleArray vertexEstimateArray;
	vertexEstimateArray = env->NewDoubleArray(16);
	double *tempVertexEstimateArray;

	Eigen::Isometry3d vertexEstimate = static_cast<g2o::VertexSE3*>(optimizer.vertex(poseId))->estimate();
	tempVertexEstimateArray = vertexEstimate.data();
	env->SetDoubleArrayRegion(vertexEstimateArray, 0, 16, tempVertexEstimateArray);
	return vertexEstimateArray;
}

JNIEXPORT void JNICALL Java_it_unibo_slam_graph_GraphBackend_removeVertexNative(JNIEnv *env, jobject obj,
		jint poseId)
{
	g2o::SparseOptimizer::Vertex* vx = optimizer.vertex(poseId);

	for(g2o::HyperGraph::EdgeSet::const_iterator it = vx->edges().begin(); it != vx->edges().end(); ++it)
		edgeWeights -= static_cast<const EdgeSE3XYZPair*>(*it)->weight();

	optimizer.removeVertex(vx);
}

JNIEXPORT void JNICALL Java_it_unibo_slam_graph_GraphBackend_addEdgeNative(JNIEnv *env, jobject obj,
		jint referenceId, jint matchingId, jdoubleArray referenceVx, jdoubleArray matchingVx, jdouble score)
{
	double tempReferenceVx[3];
	double tempMatchingVx[3];
	env->GetDoubleArrayRegion(referenceVx, 0, 3, tempReferenceVx);
	env->GetDoubleArrayRegion(matchingVx, 0, 3, tempMatchingVx);

	Eigen::Vector3d referenceVxEigen(tempReferenceVx[0], tempReferenceVx[1], tempReferenceVx[2]);
	Eigen::Vector3d matchingVxEigen(tempMatchingVx[0], tempMatchingVx[1], tempMatchingVx[2]);

	g2o::SparseOptimizer::Vertex* referenceVertex = optimizer.vertex(referenceId);
	g2o::SparseOptimizer::Vertex* matchingVertex = optimizer.vertex(matchingId);

	EdgeSE3XYZPair* edge = new EdgeSE3XYZPair();
	edge->setParameterId(0, 0);
	edge->setParameterId(1, 0);
	edge->vertices()[0] = referenceVertex;
	edge->vertices()[1] = matchingVertex;

	XYZPairMeasurement measurement;
	measurement.col(0) = referenceVxEigen;
	measurement.col(1) = matchingVxEigen;

	edge->setMeasurement(measurement);
	if (score > 0)
		edge->setWeight(score);
	edge->setPointPointMetric();
	optimizer.addEdge(edge);

	edgeWeights += edge->weight();
}

JNIEXPORT jint JNICALL Java_it_unibo_slam_graph_GraphBackend_optimizeNative(JNIEnv *env, jobject obj,
		jint maximumNumberOfIterations)
{
	action.reset();
	optimizer.initializeOptimization(-1);
	return optimizer.optimize(maximumNumberOfIterations);
}

JNIEXPORT jint JNICALL Java_it_unibo_slam_graph_GraphBackend_relativeOptimizeNative(JNIEnv *env, jobject obj,
		jintArray vSet, jint size, jint maximumNumberOfIterations)
{
	int vSetArray[size];
	env->GetIntArrayRegion(vSet, 0, size, vSetArray);

	g2o::HyperGraph::VertexSet vSetG2O;
	for (int i = 0; i < size; i++)
		vSetG2O.insert(optimizer.vertex(vSetArray[i]));

	action.reset();
	optimizer.initializeOptimization(vSetG2O, -1);
	return optimizer.optimize(maximumNumberOfIterations);
}

JNIEXPORT void JNICALL Java_it_unibo_slam_graph_GraphBackend_createRBASubsetNative(JNIEnv *env, jobject obj,
		jint originId, jint ring, jobject vSet, jobject fSet)
{
	//TODO verificare se i metodi sono corretti

	jclass vSetClass = env->GetObjectClass(vSet);
	jmethodID vSetMethod = env->GetMethodID(vSetClass, "add", "(Ljava/lang/Object;)Z");
	if (vSetMethod == 0)
	{
		LOGE("VSET METHOD NOT FOUND");
		return;
	}

	jclass fSetClass = env->GetObjectClass(fSet);
	jmethodID fSetMethod = env->GetMethodID(fSetClass, "add", "(Ljava/lang/Object;)Z");
	if (fSetMethod == 0)
	{
		LOGE("FSET METHOD NOT FOUND");
		return;
	}

	g2o::HyperGraph::Vertex* origin = optimizer.vertex(originId);
	g2o::HyperGraph::VertexSet vSetG2O;
	g2o::HyperGraph::VertexSet fSetG2O;

	vSetG2O.insert(origin);

	g2o::HyperGraph::VertexSet localSets[2];
	localSets[0] = vSetG2O;
	g2o::HyperGraph::VertexSet* lastRing = &localSets[0];
	g2o::HyperGraph::VertexSet* nextRing = &localSets[1];

	for (unsigned cr = 0; cr <= ring; ++cr)
	{
		for (g2o::HyperGraph::VertexSet::const_iterator vit = lastRing->begin(); vit != lastRing->end(); ++vit)
		{
			// Collect next ring
			for (g2o::HyperGraph::EdgeSet::iterator eit = (*vit)->edges().begin(); eit != (*vit)->edges().end(); ++eit)
			{
				for (unsigned i = 0; i < (*eit)->vertices().size(); ++i)
				{
					g2o::HyperGraph::Vertex* vi = (*eit)->vertex(i);
					if (!vSetG2O.count(vi))
					{
						if (cr == ring)
						{
							vSetG2O.insert(vi);
							fSetG2O.insert(vi);
							static_cast<g2o::SparseOptimizer::Vertex*>(vi)->setFixed(true);
						}
						else
							nextRing->insert(vi);
					}
				} // For every vx attached to the periphery
			} // For every peripheral edge
		} // For every peripheral vx

		if (nextRing->empty())
			break;

		vSetG2O.insert(nextRing->begin(), nextRing->end());
		std::swap(lastRing, nextRing);
		nextRing->clear();

	} // For every ring

	jclass integerClass = env->FindClass("java/lang/Integer");
	jmethodID integerInitMethod = env->GetMethodID(integerClass, "<init>", "(I)V");

	//TODO verificare funzionamento - passaggio da int a Integer
	for (g2o::HyperGraph::VertexSet::const_iterator vit = vSetG2O.begin(); vit != vSetG2O.end(); ++vit)
		env->CallBooleanMethod(vSet, vSetMethod, env->NewObject(integerClass, integerInitMethod, (*vit)->id()));
	for (g2o::HyperGraph::VertexSet::const_iterator fit = fSetG2O.begin(); fit != fSetG2O.end(); ++fit)
		env->CallBooleanMethod(fSet, fSetMethod, env->NewObject(integerClass, integerInitMethod, (*fit)->id()));
}

extern "C"
{
	JNIEXPORT void JNICALL Java_it_unibo_slam_main_SlamDunk_initDijkstraNative(JNIEnv *env, jobject obj);
	JNIEXPORT jboolean JNICALL Java_it_unibo_slam_main_SlamDunk_loopClosureCheck(JNIEnv *env, jobject obj,
			jintArray trackedKeyframes, jint size, jint rbaRings);
	JNIEXPORT void JNICALL Java_it_unibo_slam_main_SlamDunk_updateAdjacencyMap(JNIEnv *env, jobject obj,
			jint id);
}

g2o::HyperDijkstra dijkstra = g2o::HyperDijkstra(&optimizer);

JNIEXPORT void JNICALL Java_it_unibo_slam_main_SlamDunk_initDijkstraNative(JNIEnv *env, jobject obj)
{
	dijkstra = g2o::HyperDijkstra(&optimizer);
}

JNIEXPORT jboolean JNICALL Java_it_unibo_slam_main_SlamDunk_loopClosureCheck(JNIEnv *env, jobject obj,
		jintArray trackedKeyframes, jint size, jint rbaRings)
{
	int trackedKeyframesArray[size];
	env->GetIntArrayRegion(trackedKeyframes, 0, size, trackedKeyframesArray);

	std::vector<g2o::HyperGraph::Vertex*> trackedVertices(size);
	for (unsigned k = 0; k < trackedVertices.size(); k++)
		trackedVertices[k] = optimizer.vertex(trackedKeyframesArray[k]);

	g2o::UniformCostFunction edgeCost;
	bool isKeyframe = false;
	for (unsigned k = 0; k < trackedVertices.size() && !isKeyframe; ++k)
	{
		g2o::HyperGraph::Vertex* vx = trackedVertices[k];
		dijkstra.shortestPaths(vx, &edgeCost, rbaRings);
		const g2o::HyperGraph::VertexSet& visited = dijkstra.visited();

		for (unsigned k2 = k; k2 < trackedVertices.size() && !isKeyframe; ++k2)
			isKeyframe = (visited.count(trackedVertices[k2]) == 0);
	}

	return isKeyframe;
}

JNIEXPORT void JNICALL Java_it_unibo_slam_main_SlamDunk_updateAdjacencyMap(JNIEnv *env, jobject obj,
		jint id)
{
	g2o::HyperGraph::Vertex* vx = optimizer.vertex(id);
	g2o::HyperDijkstra::AdjacencyMapEntry entry(vx, 0, 0, std::numeric_limits< double >::max());
	dijkstra.adjacencyMap().insert(std::make_pair(entry.child(), entry));
}
