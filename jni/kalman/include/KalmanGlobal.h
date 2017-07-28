#ifndef KALMAN_GLOBAL_H
#define KALMAN_GLOBAL_H

#include "KalmanFilterHandler.h"

namespace kalman
{
	class KalmanGlobal
	{
		private:
			bool handlerInitialized;

			KalmanFilterHandler *handler;

			KalmanGlobal()
			{
				handlerInitialized = false;
			}

			~KalmanGlobal()
			{
				if (handler != NULL)
					delete handler;
			}

			KalmanGlobal(const KalmanGlobal& kalman);

			void operator=(const KalmanGlobal& kalman);

		public:
			static KalmanGlobal& getInstance()
			{
				static KalmanGlobal instance;
				return instance;
			}

			bool setHandlerPtr(KalmanFilterHandler *handler)
			{
				if (!handlerInitialized && handler != NULL)
				{
					this->handler = handler;
					handlerInitialized = true;
					return true;
				}
				else
					return false;
			}

			KalmanFilterHandler* getHandlerPtr()
			{
				return handler;
			}
	};
}

#endif // KALMAN_GLOBAL_H
