#ifndef KALMAN_FILTER_H
#define KALMAN_FILTER_H

/*#include "KalmanInput.h"

using namespace Eigen;

namespace kalman
{
	//template<typename KalmanInput, typename KalmanMeasurement>
	class KalmanFilter
	{
		protected:
			// Values used to initialize the filter matrices.
			int numStateVariables, numControlVariables, numMeasurements;

			// The matrix P.
			MatrixXf errorCovarianceBefore, errorCovarianceAfter;

			// The matrix F.
			MatrixXf stateTransitionMatrix;

			// The matrix B.
			MatrixXf controlInputMatrix;

			// The matrix Q.
			MatrixXf processNoiseCovarianceMatrix;

			// The matrix H.
			MatrixXf observationMatrix;

			// The matrix R.
			MatrixXf observationNoiseCovarianceMatrix;

		private:
			void init()
			{
				errorCovarianceBefore = MatrixXf(numStateVariables, numStateVariables);
				errorCovarianceAfter = MatrixXf(numStateVariables, numStateVariables);

				stateTransitionMatrix = MatrixXf(numStateVariables, numStateVariables);
				if (numControlVariables != 0)
					controlInputMatrix = MatrixXf(numStateVariables, numControlVariables);
				processNoiseCovarianceMatrix = MatrixXf(numStateVariables, numStateVariables);

				observationMatrix = MatrixXf(numMeasurements, numStateVariables);
				observationNoiseCovarianceMatrix = MatrixXf(numMeasurements, numMeasurements);
			}

		public:
			EIGEN_MAKE_ALIGNED_OPERATOR_NEW;

			virtual void predict(KalmanInput& input) = 0;

			KalmanFilter(int numStateVariablesIn, int numControlVariablesIn, int numMeasurementsIn)
				: numStateVariables(numStateVariablesIn), numControlVariables(numControlVariablesIn),
				  numMeasurements(numMeasurementsIn)
			{
				init();
			}

			//virtual KalmanMeasurement update(KalmanMeasurement measurement);
	};
}*/

#endif // KALMAN_FILTER_H
