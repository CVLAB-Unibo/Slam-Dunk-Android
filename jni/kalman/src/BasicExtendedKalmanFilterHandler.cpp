#include <android/log.h>
#include <time.h>

#include "kalman/include/BasicExtendedKalmanFilterHandler.h"

using namespace kalman;

#define  LOG_TAG	"kalman_time_exec"
#define  LOGI(...)	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)	__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

void BasicExtendedKalmanFilterHandler::handleData(KalmanData data)
{
	if ((data.dataType & KalmanData::ACCELERATION) == KalmanData::ACCELERATION)
	{
		iterLogAccB++;

		clock_t start = clock();
		positionKF->predict();
		positionKF->updateInertial(data.acceleration);
		clock_t end = clock();
		//LOGI("POSITION PREDICT + UPDATE BLIND: %f",  (1000.0F * (float)(end - start) / (float)CLOCKS_PER_SEC));

		iterValAccB += (1000.0F * (float)(end - start) / (float)CLOCKS_PER_SEC);

		if (iterLogAccB == 3000)
		{
			LOGI("ACC BLIND: %f",  (iterValAccB / iterLogAccB));
		}
	}

	if ((data.dataType & KalmanData::ANGULAR_VELOCITY) == KalmanData::ANGULAR_VELOCITY)
	{
		iterLogGyrB++;

		clock_t start = clock();
		orientationEKF->predict();
		orientationEKF->updateInertial(data.angularVelocity);
		positionKF->setCurrentOrientation(orientationEKF->getOrientation());
		clock_t end = clock();
		//LOGI("ORIENTATION PREDICT + UPDATE BLIND: %f",  (1000.0F * (float)(end - start) / (float)CLOCKS_PER_SEC));

		iterValGyrB += (1000.0F * (float)(end - start) / (float)CLOCKS_PER_SEC);

		if (iterLogGyrB == 3000)
		{
			LOGI("GYR BLIND: %f",  (iterValGyrB / iterLogGyrB));
		}
	}

	if ((data.dataType & KalmanData::POSITION) == KalmanData::POSITION)
	{
		iterLogAccNB++;

		clock_t start = clock();
		positionKF->predict();
		positionKF->updateVisual(data.position);
		clock_t end = clock();
		//LOGI("POSITION PREDICT + UPDATE NON-BLIND: %f",  (1000.0F * (float)(end - start) / (float)CLOCKS_PER_SEC));

		iterValAccNB += (1000.0F * (float)(end - start) / (float)CLOCKS_PER_SEC);

		if (iterLogAccNB == 300)
		{
			LOGI("ACC NON-BLIND: %f",  (iterValAccNB / iterLogAccNB));
		}
	}

	if ((data.dataType & KalmanData::ORIENTATION) == KalmanData::ORIENTATION)
	{
		iterLogGyrNB++;

		clock_t start = clock();
		orientationEKF->predict();
		AngleAxisf orientationAngleAxis(data.orientation);
		Vector3f orientationVector = orientationAngleAxis.angle() * orientationAngleAxis.axis();
		orientationEKF->updateVisual(orientationVector);
		positionKF->setCurrentOrientation(orientationEKF->getOrientation());
		clock_t end = clock();
		//LOGI("ORIENTATION PREDICT + UPDATE NON-BLIND: %f",  (1000.0F * (float)(end - start) / (float)CLOCKS_PER_SEC));

		iterValGyrNB += (1000.0F * (float)(end - start) / (float)CLOCKS_PER_SEC);

		if (iterLogGyrNB == 300)
		{
			LOGI("GYR NON-BLIND: %f",  (iterValGyrNB / iterLogGyrNB));
		}
	}
}

Isometry3f BasicExtendedKalmanFilterHandler::getCurrentPose()
{
	Isometry3f result = Isometry3f::Identity();

	Vector3f position = positionKF->getPosition();
	Vector3f orientationVector = orientationEKF->getOrientation();
	float orientationAngle = orientationVector.norm();
	Matrix3f imuToCamera;
	imuToCamera << 	1, 	 0,	 0,
					0,	-1,	 0,
					0,	 0,	-1;
	Matrix3f orientationMatrix;
	//orientationVector.y() = -orientationVector.y();
	//orientationVector.z() = -orientationVector.z();
	orientationMatrix = /* imuToCamera */ AngleAxisf(orientationAngle, (orientationAngle == 0) ? Vector3f(1, 0, 0) :
						(orientationVector / orientationAngle).eval()).toRotationMatrix();

	result.matrix().topLeftCorner<3, 3>() << orientationMatrix;
	result.matrix()(0, 3) = position.x();
	result.matrix()(1, 3) = position.y();
	result.matrix()(2, 3) = position.z();

	return result;
}
