#ifndef KALMAN_FILTER_HANDLER_H
#define KALMAN_FILTER_HANDLER_H

#include <Eigen/Core>
#include <Eigen/Geometry>

#include "KalmanData.h"

using namespace Eigen;

namespace kalman
{
	class KalmanFilterHandler
	{
		public:
			virtual void setVisualObservationNoise(float noiseValue) = 0;

			virtual void handleData(KalmanData data) = 0;

			virtual Isometry3f getCurrentPose() = 0;
	};
}

#endif // KALMAN_FILTER_HANDLER_H
