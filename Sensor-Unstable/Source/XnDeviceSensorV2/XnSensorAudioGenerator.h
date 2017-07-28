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
#ifndef __XN_SENSOR_AUDIO_GENERATOR_H__
#define __XN_SENSOR_AUDIO_GENERATOR_H__

//---------------------------------------------------------------------------
// Includes
//---------------------------------------------------------------------------
#include "XnSensorGenerator.h"
#include "XnExportedSensorGenerator.h"
#include <XnListT.h>

//---------------------------------------------------------------------------
// Types
//---------------------------------------------------------------------------
// disable the "inherits via dominance" warning. This is exactly what we want.
#pragma warning (push)
#pragma warning (disable: 4250)

class XnSensorAudioGenerator : 
	public XnSensorGenerator,
	virtual public xn::ModuleAudioGenerator
{
public:
	XnSensorAudioGenerator(xn::Context& context, xn::Device& sensor, XnDeviceBase* pSensor, const XnChar* strStreamName);

	XnStatus Init();

	XnBool IsCapabilitySupported(const XnChar* strCapabilityName);

	const void* GetData() { return XnSensorGenerator::GetData(); }
	XnUChar* GetAudioBuffer();
	XnUInt32 GetSupportedWaveOutputModesCount();
	XnStatus GetSupportedWaveOutputModes(XnWaveOutputMode aSupportedModes[], XnUInt32& nCount);
	XnStatus SetWaveOutputMode(const XnWaveOutputMode& OutputMode);
	XnStatus GetWaveOutputMode(XnWaveOutputMode& OutputMode);
	XnStatus RegisterToWaveOutputModeChanges(XnModuleStateChangedHandler handler, void* pCookie, XnCallbackHandle& hCallback);
	void UnregisterFromWaveOutputModeChanges(XnCallbackHandle hCallback);

protected:
	virtual void FilterProperties(XnActualPropertiesHash* pHash);

private:
	typedef XnListT<XnWaveOutputMode> XnWaveOutputModeList;

	XnWaveOutputModeList m_SupportedModes;
};

class XnExportedSensorAudioGenerator : public XnExportedSensorGenerator
{
public:
	XnExportedSensorAudioGenerator();

protected:
	virtual XnStatus IsSupportedForDevice(xn::Context& context, xn::NodeInfo& sensorInfo, XnBool* pbSupported);
	virtual XnSensorGenerator* CreateGenerator(xn::Context& context, xn::Device& sensor, XnDeviceBase* pSensor, const XnChar* strStreamName);
};

#pragma warning (pop)

#endif // __XN_SENSOR_AUDIO_GENERATOR_H__