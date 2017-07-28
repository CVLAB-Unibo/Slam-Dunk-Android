#ifndef BASIC_EXTENDED_KALMAN_FILTER_HANDLER_H
#define BASIC_EXTENDED_KALMAN_FILTER_HANDLER_H

#include "KalmanFilterHandler.h"
#include "PositionLinearKalmanFilter.h"
#include "OrientationExtendedKalmanFilter.h"

namespace kalman
{
	class BasicExtendedKalmanFilterHandler : public KalmanFilterHandler
	{
		private:
			PositionLinearKalmanFilter *positionKF;

			OrientationExtendedKalmanFilter *orientationEKF;

			int iterLogAccB;
			int iterLogAccNB;
			int iterLogGyrB;
			int iterLogGyrNB;

			float iterValAccB;
			float iterValAccNB;
			float iterValGyrB;
			float iterValGyrNB;

		public:
			BasicExtendedKalmanFilterHandler(float accelDeltaT, float gyroDeltaT, Vector3f gravity)
			{
				//TODO tuning parametri
				positionKF = new PositionLinearKalmanFilter(accelDeltaT, gravity);
				positionKF->setInertialObservationNoise(0.1);
				orientationEKF = new OrientationExtendedKalmanFilter(gyroDeltaT);
				orientationEKF->setInertialObservationNoise(0.001);

				iterLogAccB = 0;
				iterLogAccNB = 0;
				iterLogGyrB = 0;
				iterLogGyrNB = 0;

				iterValAccB = 0.0F;
				iterValAccNB = 0.0F;
				iterValGyrB = 0.0F;
				iterValGyrNB = 0.0F;
			}

			~BasicExtendedKalmanFilterHandler()
			{
				delete positionKF;
				delete orientationEKF;
			}

			virtual void setVisualObservationNoise(float noiseValue)
			{
				positionKF->setVisualObservationNoise(noiseValue);
				orientationEKF->setVisualObservationNoise(noiseValue);
			}

			virtual void handleData(KalmanData data);

			virtual Isometry3f getCurrentPose();
	};
}

#endif // BASIC_EXTENDED_KALMAN_FILTER_HANDLER_H
