/****************************************************************************
*                                                                           *
*  PrimeSense Sensor 5.x Alpha                                              *
*  Copyright (C) 2011 PrimeSense Ltd.                                       *
*                                                                           *
*  This file is part of PrimeSense Sensor.                                  *
*                                                                           *
*  PrimeSense Sensor is free software: you can redistribute it and/or modify*
*  it under the terms of the GNU Lesser General Public License as published *
*  by the Free Software Foundation, either version 3 of the License, or     *
*  (at your option) any later version.                                      *
*                                                                           *
*  PrimeSense Sensor is distributed in the hope that it will be useful,     *
*  but WITHOUT ANY WARRANTY; without even the implied warranty of           *
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the             *
*  GNU Lesser General Public License for more details.                      *
*                                                                           *
*  You should have received a copy of the GNU Lesser General Public License *
*  along with PrimeSense Sensor. If not, see <http://www.gnu.org/licenses/>.*
*                                                                           *
****************************************************************************/
#ifndef __XN_SENSOR_FIXED_PARAMS_H__
#define __XN_SENSOR_FIXED_PARAMS_H__

//---------------------------------------------------------------------------
// Includes
//---------------------------------------------------------------------------
#include <XnStreamParams.h>
#include "XnDeviceSensor.h"

//---------------------------------------------------------------------------
// Forward Declarations
//---------------------------------------------------------------------------
class XnSensorFirmware;
struct XnDevicePrivateData;
typedef struct XnDevicePrivateData XnDevicePrivateData;

//---------------------------------------------------------------------------
// XnSensorFixedParams class
//---------------------------------------------------------------------------
class XnSensorFixedParams
{
public:
	XnSensorFixedParams(XnDevicePrivateData* pDevicePrivateData);

	XnStatus Init();

	inline XnUInt16 GetDepthCmosI2CBus() const { return m_nSensorDepthCMOSI2CBus; }
	inline XnUInt16 GetDepthCmosI2CSlaveAddress() const { return m_nSensorDepthCMOSI2CSlaveAddress; }
	inline XnUInt16 GetImageCmosI2CBus() const { return m_nSensorImageCMOSI2CBus; }
	inline XnUInt16 GetImageCmosI2CSlaveAddress() const { return m_nSensorImageCMOSI2CSlaveAddress; }

	inline XnDepthPixel GetZeroPlaneDistance() const { return m_nZeroPlaneDistance; }
	inline XnDouble GetZeroPlanePixelSize() const { return m_dZeroPlanePixelSize; }
	inline XnDouble GetEmitterDCmosDistance() const { return m_dEmitterDCmosDistance; }
	inline XnDouble GetDCmosRCmosDistance() const { return m_dDCmosRCmosDistance; }

	inline const XnChar* GetSensorSerial() const { return m_strSensorSerial; }

	inline XnUInt32 GetImageCmosType() const { return m_nImageCmosType; }

	inline const XnChar* GetDeviceName() const { return m_deviceInfo.strDeviceName; }
	inline const XnChar* GetVendorData() const { return m_deviceInfo.strVendorData; }
	inline const XnChar* GetPlatformString() const { return m_strPlatformString; }

private:
	XnDevicePrivateData* m_pDevicePrivateData;

	XnUInt16 m_nSensorDepthCMOSI2CBus;
	XnUInt16 m_nSensorDepthCMOSI2CSlaveAddress;
	XnUInt16 m_nSensorImageCMOSI2CBus;
	XnUInt16 m_nSensorImageCMOSI2CSlaveAddress;

	XnDepthPixel m_nZeroPlaneDistance;
	XnDouble m_dZeroPlanePixelSize;
	XnDouble m_dEmitterDCmosDistance;
	XnDouble m_dDCmosRCmosDistance;

	XnUInt32 m_nImageCmosType;

	XnChar m_strSensorSerial[XN_DEVICE_MAX_STRING_LENGTH];
	XnDeviceInformation m_deviceInfo;
	XnChar m_strPlatformString[XN_DEVICE_MAX_STRING_LENGTH];
};

#endif //__XN_SENSOR_FIXED_PARAMS_H__