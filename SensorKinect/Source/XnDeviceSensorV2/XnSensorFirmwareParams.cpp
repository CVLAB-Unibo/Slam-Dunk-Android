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
//---------------------------------------------------------------------------
// Includes
//---------------------------------------------------------------------------
#include "XnSensorFirmwareParams.h"

//---------------------------------------------------------------------------
// Code
//---------------------------------------------------------------------------
XnSensorFirmwareParams::XnSensorFirmwareParams(XnFirmwareInfo* pInfo, XnFirmwareCommands* pCommands) :
	m_AllFirmwareParams(),
	/* Member						   Name							Firmware Param								Min Valid Version     Max Valid Version     Value if wrong version */
	/* ====================		       ========================		=====================================		====================  ====================  ====================== */
	m_FrameSyncEnabled("FrameSync"),
	m_RegistrationEnabled("Registration"),
	m_Stream0Mode("Stream0Mode"),
	m_Stream1Mode("Stream1Mode"),
	m_Stream2Mode("Stream2Mode"),
	m_AudioStereo("AudioStereo"),
	m_AudioSampleRate("AudioSampleRate"),
	m_AudioLeftChannelGain("AudioLeftChannelGain"),
	m_AudioRightChannelGain("AudioRightChannelGain"),
	m_ImageFormat("ImageFormat"),
	m_ImageResolution("ImageResolution"),
	m_ImageFPS("ImageFPS"),
	m_ImageQuality("ImageQuality"),
	m_ImageFlickerDetection("ImageFlicker"),
	m_ImageCropSizeX("ImageCropSizeX"),
	m_ImageCropSizeY("ImageCropSizeY"),
	m_ImageCropOffsetX("ImageCropOffsetX"),
	m_ImageCropOffsetY("ImageCropOffsetY"),
	m_ImageCropEnabled("ImageCropEnabled"),
	m_DepthFormat("DepthFormat"),
	m_DepthResolution("DepthResolution"),
	m_DepthFPS("DepthFPS"),
	m_DepthGain("DepthGain"),
	m_DepthHoleFilter("DepthHoleFilter"),
	m_DepthMirror("DepthMirror"),
	m_DepthDecimation("DepthDecimation"),
	m_DepthCropSizeX("DepthCropSizeX"),
	m_DepthCropSizeY("DepthCropSizeY"),
	m_DepthCropOffsetX("DepthCropOffsetX"),
	m_DepthCropOffsetY("DepthCropOffsetY"),
	m_DepthCropEnabled("DepthCropEnabled"),
	m_IRFormat("IRFormat"),
	m_IRResolution("IRResolution"),
	m_IRFPS("IRFPS"),
	m_IRCropSizeX("IRCropSizeX"),
	m_IRCropSizeY("IRCropSizeY"),
	m_IRCropOffsetX("IRCropOffsetX"),
	m_IRCropOffsetY("IRCropOffsetY"),
	m_IRCropEnabled("IRCropEnabled"),
	m_DepthWhiteBalance("DepthWhiteBalance"),
	m_ImageMirror("ImageMirror"),
	m_IRMirror("IRMirror"),
	m_ReferenceResolution("ReferenceResolution", 0, "Firmware"),
	m_GMCMode("GMCMode"),
	m_ImageSharpness("ImageSharpness"),
	m_ImageAutoWhiteBalance("ImageAutoWhiteBalance"),
	m_ImageColorTemperature("ImageColorTemperature"),
	m_ImageBacklightCompensation("ImageBacklightCompensation"),
	m_ImageAutoExposure("ImageAutoExposure"),
	m_ImageExposureBar("ImageExposureBar"),
	m_ImageLowLightCompensation("ImageLowLightCompensation"),
	m_ImageGain("ImageGain"),
	m_pInfo(pInfo),
	m_pCommands(pCommands),
	m_bInTransaction(FALSE)
{
	m_ReferenceResolution.SetLogSeverity(XN_LOG_VERBOSE);
}

XnSensorFirmwareParams::~XnSensorFirmwareParams()
{
}

XnStatus XnSensorFirmwareParams::Init()
{
	XnStatus nRetVal = XN_STATUS_OK;

	/*								Property					Param										MinVersion				MaxVersion					ValueIfNotSupported */
	/*								======================		=======================================		====================	====================		=================== */
	nRetVal = AddFirmwareParam(		m_FrameSyncEnabled,			PARAM_GENERAL_FRAME_SYNC);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_RegistrationEnabled,		PARAM_GENERAL_REGISTRATION_ENABLE);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_Stream0Mode,				PARAM_GENERAL_STREAM0_MODE);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_Stream1Mode,				PARAM_GENERAL_STREAM1_MODE);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareAudioParam(m_Stream2Mode,				PARAM_GENERAL_STREAM2_MODE);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareAudioParam(m_AudioStereo,				PARAM_AUDIO_STEREO_MODE);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareAudioParam(m_AudioSampleRate,			PARAM_AUDIO_SAMPLE_RATE);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareAudioParam(m_AudioLeftChannelGain,		PARAM_AUDIO_LEFT_CHANNEL_VOLUME_LEVEL);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareAudioParam(m_AudioRightChannelGain,	PARAM_AUDIO_RIGHT_CHANNEL_VOLUME_LEVEL);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_ImageFormat,				PARAM_IMAGE_FORMAT);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_ImageResolution,			PARAM_IMAGE_RESOLUTION);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_ImageFPS,					PARAM_IMAGE_FPS);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_ImageQuality,				PARAM_IMAGE_QUALITY);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_ImageFlickerDetection,	PARAM_IMAGE_FLICKER_DETECTION);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_ImageCropSizeX,			PARAM_IMAGE_CROP_SIZE_X,					XN_SENSOR_FW_VER_5_0,	XN_SENSOR_FW_VER_UNKNOWN,	0);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_ImageCropSizeY,			PARAM_IMAGE_CROP_SIZE_Y,					XN_SENSOR_FW_VER_5_0,	XN_SENSOR_FW_VER_UNKNOWN,	0);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_ImageCropOffsetX,			PARAM_IMAGE_CROP_OFFSET_X,					XN_SENSOR_FW_VER_5_0,	XN_SENSOR_FW_VER_UNKNOWN,	0);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_ImageCropOffsetY,			PARAM_IMAGE_CROP_OFFSET_Y,					XN_SENSOR_FW_VER_5_0,	XN_SENSOR_FW_VER_UNKNOWN,	0);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_ImageCropEnabled,			PARAM_IMAGE_CROP_ENABLE,					XN_SENSOR_FW_VER_5_0,	XN_SENSOR_FW_VER_UNKNOWN,	FALSE);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_DepthFormat,				PARAM_DEPTH_FORMAT);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_DepthResolution,			PARAM_DEPTH_RESOLUTION);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_DepthFPS,					PARAM_DEPTH_FPS);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_DepthGain,				PARAM_DEPTH_AGC);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_DepthHoleFilter,			PARAM_DEPTH_HOLE_FILTER);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_DepthMirror,				PARAM_DEPTH_MIRROR,							XN_SENSOR_FW_VER_5_0,	XN_SENSOR_FW_VER_UNKNOWN,	FALSE);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_DepthDecimation,			PARAM_DEPTH_DECIMATION);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_DepthCropSizeX,			PARAM_DEPTH_CROP_SIZE_X,					XN_SENSOR_FW_VER_5_0,	XN_SENSOR_FW_VER_UNKNOWN,	0);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_DepthCropSizeY,			PARAM_DEPTH_CROP_SIZE_Y,					XN_SENSOR_FW_VER_5_0,	XN_SENSOR_FW_VER_UNKNOWN,	0);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_DepthCropOffsetX,			PARAM_DEPTH_CROP_OFFSET_X,					XN_SENSOR_FW_VER_5_0,	XN_SENSOR_FW_VER_UNKNOWN,	0);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_DepthCropOffsetY,			PARAM_DEPTH_CROP_OFFSET_Y,					XN_SENSOR_FW_VER_5_0,	XN_SENSOR_FW_VER_UNKNOWN,	0);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_DepthCropEnabled,			PARAM_DEPTH_CROP_ENABLE,					XN_SENSOR_FW_VER_5_0,	XN_SENSOR_FW_VER_UNKNOWN,	FALSE);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_IRFormat,					PARAM_IR_FORMAT);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_IRResolution,				PARAM_IR_RESOLUTION);
	XN_IS_STATUS_OK(nRetVal);								
	nRetVal = AddFirmwareParam(		m_IRFPS,					PARAM_IR_FPS);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_IRCropSizeX,				PARAM_IR_CROP_SIZE_X,						XN_SENSOR_FW_VER_5_0,	XN_SENSOR_FW_VER_UNKNOWN,	0);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_IRCropSizeY,				PARAM_IR_CROP_SIZE_Y,						XN_SENSOR_FW_VER_5_0,	XN_SENSOR_FW_VER_UNKNOWN,	0);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_IRCropOffsetX,			PARAM_IR_CROP_OFFSET_X,						XN_SENSOR_FW_VER_5_0,	XN_SENSOR_FW_VER_UNKNOWN,	0);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_IRCropOffsetY,			PARAM_IR_CROP_OFFSET_Y,						XN_SENSOR_FW_VER_5_0,	XN_SENSOR_FW_VER_UNKNOWN,	0);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_IRCropEnabled,			PARAM_IR_CROP_ENABLE,						XN_SENSOR_FW_VER_5_0,	XN_SENSOR_FW_VER_UNKNOWN,	FALSE);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_DepthWhiteBalance,		PARAM_DEPTH_WHITE_BALANCE_ENABLE,			XN_SENSOR_FW_VER_4_0,	XN_SENSOR_FW_VER_UNKNOWN,	FALSE);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_ImageMirror,				PARAM_IMAGE_MIRROR,							XN_SENSOR_FW_VER_5_0,	XN_SENSOR_FW_VER_UNKNOWN,	FALSE);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_IRMirror,					PARAM_IR_MIRROR,							XN_SENSOR_FW_VER_5_0,	XN_SENSOR_FW_VER_UNKNOWN,	FALSE);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_GMCMode,					PARAM_DEPTH_GMC_MODE,						XN_SENSOR_FW_VER_3_0,	XN_SENSOR_FW_VER_UNKNOWN,	FALSE);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_ImageSharpness,			PARAM_IMAGE_SHARPNESS,						XN_SENSOR_FW_VER_5_4,	XN_SENSOR_FW_VER_UNKNOWN,	50);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_ImageAutoWhiteBalance,	PARAM_IMAGE_AUTO_WHITE_BALANCE_MODE,		XN_SENSOR_FW_VER_5_4,	XN_SENSOR_FW_VER_UNKNOWN,	FALSE);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_ImageColorTemperature,	PARAM_IMAGE_COLOR_TEMPERATURE,				XN_SENSOR_FW_VER_5_4,	XN_SENSOR_FW_VER_UNKNOWN,	0);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_ImageBacklightCompensation,PARAM_IMAGE_BACK_LIGHT_COMPENSATION,		XN_SENSOR_FW_VER_5_4,	XN_SENSOR_FW_VER_UNKNOWN,	FALSE);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_ImageAutoExposure,		PARAM_IMAGE_AUTO_EXPOSURE_MODE,				XN_SENSOR_FW_VER_5_4,	XN_SENSOR_FW_VER_UNKNOWN,	FALSE);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_ImageExposureBar,			PARAM_IMAGE_EXPOSURE_BAR,				XN_SENSOR_FW_VER_5_4,	XN_SENSOR_FW_VER_UNKNOWN,	0);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_ImageLowLightCompensation,PARAM_IMAGE_LOW_LIGHT_COMPENSATION_MODE,	XN_SENSOR_FW_VER_5_4,	XN_SENSOR_FW_VER_UNKNOWN,	FALSE);
	XN_IS_STATUS_OK(nRetVal);
	nRetVal = AddFirmwareParam(		m_ImageGain,				PARAM_IMAGE_AGC,							XN_SENSOR_FW_VER_5_4,	XN_SENSOR_FW_VER_UNKNOWN,	0);
	XN_IS_STATUS_OK(nRetVal);

	// override some props
	m_ImageResolution.UpdateSetCallback(SetImageResolutionCallback, this);
	m_ImageFormat.UpdateSetCallback(SetImageFormatCallback, this);

	// register for some interesting changes
	XnCallbackHandle hCallbackDummy;
	nRetVal = m_Stream0Mode.OnChangeEvent().Register(ReferenceResolutionPropertyValueChanged, this, &hCallbackDummy);
	XN_IS_STATUS_OK(nRetVal);

	nRetVal = m_Stream1Mode.OnChangeEvent().Register(ReferenceResolutionPropertyValueChanged, this, &hCallbackDummy);
	XN_IS_STATUS_OK(nRetVal);

	nRetVal = m_IRResolution.OnChangeEvent().Register(ReferenceResolutionPropertyValueChanged, this, &hCallbackDummy);
	XN_IS_STATUS_OK(nRetVal);

	nRetVal = m_DepthFPS.OnChangeEvent().Register(ReferenceResolutionPropertyValueChanged, this, &hCallbackDummy);
	XN_IS_STATUS_OK(nRetVal);

	nRetVal = RecalculateReferenceResolution();
	XN_IS_STATUS_OK(nRetVal);

	return (XN_STATUS_OK);
}

void XnSensorFirmwareParams::Free()
{
	m_AllFirmwareParams.Clear();
}

XnStatus XnSensorFirmwareParams::AddFirmwareParam(XnActualIntProperty& Property, XnUInt16 nFirmwareParam, XnFWVer nMinVer /* = XN_SENSOR_FW_VER_UNKNOWN */, XnFWVer nMaxVer /* = XN_SENSOR_FW_VER_UNKNOWN */, XnUInt16 nValueIfNotSupported /* = 0 */)
{
	XnStatus nRetVal = XN_STATUS_OK;

	XnFirmwareParam param;
	param.pProperty = &Property;
	param.nFirmwareParam = nFirmwareParam;
	param.MinVer = nMinVer;
	param.MaxVer = nMaxVer;
	param.nValueIfNotSupported = nValueIfNotSupported;

	nRetVal = m_AllFirmwareParams.Set(&Property, param);
	XN_IS_STATUS_OK(nRetVal);

	XnChar csNewName[XN_DEVICE_MAX_STRING_LENGTH];
	sprintf(csNewName, "%s (%d)", Property.GetName(), nFirmwareParam);

	Property.UpdateName("Firmware", csNewName);
	Property.SetLogSeverity(XN_LOG_VERBOSE);
	Property.SetAlwaysSet(TRUE);
	Property.UpdateSetCallback(SetFirmwareParamCallback, this);

	return (XN_STATUS_OK);
}

XnStatus XnSensorFirmwareParams::AddFirmwareAudioParam(XnActualIntProperty& Property, XnUInt16 nFirmwareParam, XnFWVer nMinVer /* = XN_SENSOR_FW_VER_3_0 */, XnFWVer nMaxVer /* = XN_SENSOR_FW_VER_UNKNOWN */, XnUInt16 nValueIfNotSupported /* = 0 */)
{
	XnStatus nRetVal = XN_STATUS_OK;
	
	nRetVal = AddFirmwareParam(Property, nFirmwareParam, nMinVer, nMaxVer, nValueIfNotSupported);
	XN_IS_STATUS_OK(nRetVal);

	Property.UpdateSetCallback(SetFirmwareAudioParamCallback, this);
	
	return (XN_STATUS_OK);
}

XnStatus XnSensorFirmwareParams::UpdateAllProperties()
{
	XnStatus nRetVal = XN_STATUS_OK;

	xnLogVerbose(XN_MASK_DEVICE_SENSOR, "Reading all params from firmware...");

	for (XnFirmwareParamsHash::Iterator it = m_AllFirmwareParams.begin(); it != m_AllFirmwareParams.end(); ++it)
	{
		XnFirmwareParam& param = it.Value();
		nRetVal = UpdateProperty(&param);
		XN_IS_STATUS_OK(nRetVal);
	}

	xnLogVerbose(XN_MASK_DEVICE_SENSOR, "Firmware params were updated.");

	return (XN_STATUS_OK);
}

XnStatus XnSensorFirmwareParams::StartTransaction()
{
	if (m_bInTransaction)
	{
		return XN_STATUS_ERROR;
	}

	m_bInTransaction = TRUE;
	m_Transaction.Clear();
	m_TransactionOrder.Clear();

	return (XN_STATUS_OK);
}

XnStatus XnSensorFirmwareParams::CommitTransaction()
{
	XnStatus nRetVal = XN_STATUS_OK;
	
	if (!m_bInTransaction)
	{
		return XN_STATUS_ERROR;
	}

	// we are no longer in transaction, even if we fail to commit.
	m_bInTransaction = FALSE;

	for (XnActualIntPropertyList::Iterator it = m_TransactionOrder.begin(); it != m_TransactionOrder.end(); ++it)
	{
		XnActualIntProperty* pProp = *it;

		XnUInt32 nValue;
		nRetVal = m_Transaction.Get(pProp, nValue);
		XN_IS_STATUS_OK(nRetVal);

		nRetVal = SetFirmwareParamImpl(pProp, nValue);
		XN_IS_STATUS_OK(nRetVal);
	}

	m_Transaction.Clear();
	m_TransactionOrder.Clear();
	
	return (XN_STATUS_OK);
}

XnStatus XnSensorFirmwareParams::CommitTransactionAsBatch()
{
	XnStatus nRetVal = XN_STATUS_OK;

	if (!m_bInTransaction)
	{
		return XN_STATUS_ERROR;
	}

	// we are no longer in transaction, even if we fail to commit.
	m_bInTransaction = FALSE;

	if (m_TransactionOrder.Size() != 0)
	{
		XnUInt32 nMaxCount = m_TransactionOrder.Size();
		XnInnerParamData* pParams;
		XN_VALIDATE_CALLOC(pParams, XnInnerParamData, nMaxCount);

		XnChar strLogMessage[1024];
		XnUInt32 nMaxLength = 1024;
		XnUInt32 nLength = 0;
		XnUInt32 nChars;
		xnOSStrFormat(strLogMessage + nLength, nMaxLength - nLength, &nChars, "Setting firmware params:\n\t");
		nLength += nChars;

		XnUInt32 nCount = 0;

		for (XnActualIntPropertyList::Iterator it = m_TransactionOrder.begin(); it != m_TransactionOrder.end(); ++it)
		{
			XnActualIntProperty* pProp = *it;

			XnUInt32 nValue;
			nRetVal = m_Transaction.Get(pProp, nValue);
			if (nRetVal != XN_STATUS_OK)
			{
				xnOSFree(pParams);
				return (nRetVal);
			}

			XnFirmwareParam* pParam;
			nRetVal = CheckFirmwareParam(pProp, nValue, &pParam);
			if (nRetVal != XN_STATUS_OK)
			{
				xnOSFree(pParams);
				return (nRetVal);
			}

			if (pParam != NULL)
			{
				xnOSStrFormat(strLogMessage + nLength, nMaxLength - nLength, &nChars, "%s = %u\n\t", pProp->GetName(), nValue);
				nLength += nChars;

				pParams[nCount].nParam = pParam->nFirmwareParam;
				pParams[nCount].nValue = (XnUInt16)nValue;
				nCount++;
			}
		}

		xnLogVerbose(XN_MASK_SENSOR_PROTOCOL, "%s", strLogMessage);

		// set all params
		nRetVal = m_pCommands->SetMultipleFirmwareParams(pParams, nCount);
		xnOSFree(pParams);
		XN_IS_STATUS_OK(nRetVal);

		// and update their props
		for (XnActualIntPropertyList::Iterator it = m_TransactionOrder.begin(); it != m_TransactionOrder.end(); ++it)
		{
			XnActualIntProperty* pProp = *it;

			XnUInt32 nValue;
			nRetVal = m_Transaction.Get(pProp, nValue);
			XN_IS_STATUS_OK(nRetVal);

			nRetVal = pProp->UnsafeUpdateValue(nValue);
			XN_IS_STATUS_OK(nRetVal);
		}
	}

	m_Transaction.Clear();
	m_TransactionOrder.Clear();

	return (XN_STATUS_OK);
}

XnStatus XnSensorFirmwareParams::RollbackTransaction()
{
	if (!m_bInTransaction)
	{
		return XN_STATUS_ERROR;
	}

	m_Transaction.Clear();
	m_TransactionOrder.Clear();
	m_bInTransaction = FALSE;

	return (XN_STATUS_OK);
}

XnStatus XnSensorFirmwareParams::UpdateProperty(XnFirmwareParam* pParam)
{
	XnStatus nRetVal = XN_STATUS_OK;
	
	XnUInt16 nNewValue;

	// check version
	if ((pParam->MinVer != XN_SENSOR_FW_VER_UNKNOWN && m_pInfo->nFWVer < pParam->MinVer) ||
		(pParam->MaxVer != XN_SENSOR_FW_VER_UNKNOWN && m_pInfo->nFWVer > pParam->MaxVer))
	{
		// version not supported
		nNewValue = pParam->nValueIfNotSupported;
	}
	else
	{
		// Read value from firmware
		nRetVal = m_pCommands->GetFirmwareParam(pParam->nFirmwareParam, &nNewValue);
		XN_IS_STATUS_OK(nRetVal);
	}

	// update value if needed
	if (nNewValue != pParam->pProperty->GetValue())
	{
		// update base (don't call our function, so that it won't update firmware)
		nRetVal = pParam->pProperty->UnsafeUpdateValue(nNewValue);
		XN_IS_STATUS_OK(nRetVal);
	}

	return (XN_STATUS_OK);
}

XnStatus XnSensorFirmwareParams::SetFirmwareParam(XnActualIntProperty* pProperty, XnUInt64 nValue)
{
	XnStatus nRetVal = XN_STATUS_OK;
	
	if (m_bInTransaction)
	{
		nRetVal = m_Transaction.Set(pProperty, (XnUInt32)nValue);
		XN_IS_STATUS_OK(nRetVal);

		nRetVal = m_TransactionOrder.AddLast(pProperty);
		XN_IS_STATUS_OK(nRetVal);
	}
	else
	{
		nRetVal = SetFirmwareParamImpl(pProperty, nValue);
		XN_IS_STATUS_OK(nRetVal);
	}
	
	return (XN_STATUS_OK);
}

XnStatus XnSensorFirmwareParams::SetFirmwareAudioParam(XnActualIntProperty* pProperty, XnUInt64 nValue)
{
	XnStatus nRetVal = XN_STATUS_OK;

	// check if audio is not supported, and trying to change the value
	if (!m_pInfo->bAudioSupported && nValue != pProperty->GetValue())
	{
		return (XN_STATUS_DEVICE_UNSUPPORTED_PARAMETER);
	}

	nRetVal = SetFirmwareParam(pProperty, nValue);
	XN_IS_STATUS_OK(nRetVal);
	
	return (XN_STATUS_OK);
}

XnStatus XnSensorFirmwareParams::SetImageResolution(XnUInt64 nValue)
{
	XnStatus nRetVal = XN_STATUS_OK;

	if (m_pInfo->nFWVer < XN_SENSOR_FW_VER_5_4)
	{
		switch (nValue)
		{
		case XN_RESOLUTION_QVGA:
		case XN_RESOLUTION_VGA:
			break;
		case XN_RESOLUTION_SXGA:
			// --avin mod--
			// Removed to enable 1280x1024 Image
			/*	
			if (m_pInfo->nFWVer < XN_SENSOR_FW_VER_5_3)
			{
				XN_LOG_WARNING_RETURN(XN_STATUS_IO_INVALID_STREAM_IMAGE_RESOLUTION, XN_MASK_DEVICE_SENSOR, "Image resolution is not supported by this firmware!");
			}
			*/
			break;
		case XN_RESOLUTION_UXGA:
			if (m_pInfo->nFWVer < XN_SENSOR_FW_VER_5_1)
			{
				XN_LOG_WARNING_RETURN(XN_STATUS_IO_INVALID_STREAM_IMAGE_RESOLUTION, XN_MASK_DEVICE_SENSOR, "Image resolution is not supported by this firmware!");
			}
			break;
		default:
			XN_LOG_WARNING_RETURN(XN_STATUS_DEVICE_BAD_PARAM, XN_MASK_DEVICE_SENSOR, "Unsupported image resolution: %d", nValue);
		}
	}

	nRetVal = SetFirmwareParam(&m_ImageResolution, nValue);
	XN_IS_STATUS_OK(nRetVal);
	
	return (XN_STATUS_OK);
}

XnStatus XnSensorFirmwareParams::SetImageFormat(XnUInt64 nValue)
{
	XnStatus nRetVal = XN_STATUS_OK;
	
	if (nValue == XN_IO_IMAGE_FORMAT_UNCOMPRESSED_BAYER)
	{
		nValue = XN_IO_IMAGE_FORMAT_BAYER;
	}

	nRetVal = SetFirmwareParam(&m_ImageFormat, nValue);
	XN_IS_STATUS_OK(nRetVal);
	
	return (XN_STATUS_OK);
}

XnStatus XnSensorFirmwareParams::CheckFirmwareParam(XnActualIntProperty* pProperty, XnUInt64 nValue, XnFirmwareParam** ppParam)
{
	XnStatus nRetVal = XN_STATUS_OK;

	// find the property in the hash
	XnFirmwareParam* pParam;
	nRetVal = m_AllFirmwareParams.Get(pProperty, pParam);
	XN_IS_STATUS_OK(nRetVal);

	*ppParam = NULL;

	// check version
	if ((pParam->MinVer != XN_SENSOR_FW_VER_UNKNOWN && m_pInfo->nFWVer < pParam->MinVer) ||
		(pParam->MaxVer != XN_SENSOR_FW_VER_UNKNOWN && m_pInfo->nFWVer > pParam->MaxVer))
	{
		// we only raise an error when trying to change the value...
		if (nValue != pParam->nValueIfNotSupported)
		{
			return (XN_STATUS_DEVICE_UNSUPPORTED_PARAMETER);
		}
	}
	else
	{
		*ppParam = pParam;
	}

	return XN_STATUS_OK;
}

XnStatus XnSensorFirmwareParams::SetFirmwareParamImpl(XnActualIntProperty* pProperty, XnUInt64 nValue)
{
	XnStatus nRetVal = XN_STATUS_OK;

	XnFirmwareParam* pParam;
	nRetVal = CheckFirmwareParam(pProperty, nValue, &pParam);
	XN_IS_STATUS_OK(nRetVal);

	if (pParam != NULL)
	{
		// update firmware
		nRetVal = m_pCommands->SetFirmwareParam(pParam->nFirmwareParam, (XnUInt16)nValue); 
		XN_IS_STATUS_OK(nRetVal);

		// update property
		nRetVal = pParam->pProperty->UnsafeUpdateValue(nValue);
		XN_IS_STATUS_OK(nRetVal);
	}

	return (XN_STATUS_OK);
}

XnStatus XnSensorFirmwareParams::SetStreamMode(XnActualIntProperty* pProperty, XnUInt64 nValue)
{
	XnStatus nRetVal = XN_STATUS_OK;
	
	// we require that every change to mode will go through OFF
	if (nValue != XN_VIDEO_STREAM_OFF && pProperty->GetValue() != XN_VIDEO_STREAM_OFF)
	{
		XN_LOG_WARNING_RETURN(XN_STATUS_DEVICE_BAD_PARAM, XN_MASK_DEVICE_SENSOR, "Firmware stream is already in use!");
	}

	// OK, set it
	nRetVal = SetFirmwareParam(pProperty, nValue);
	XN_IS_STATUS_OK(nRetVal);
	
	return (XN_STATUS_OK);
}

XnStatus XnSensorFirmwareParams::RecalculateReferenceResolution()
{
	XnStatus nRetVal = XN_STATUS_OK;
	
	// by default, the 1.3 MP reference is used
	XnResolutions nRes = XN_RESOLUTION_SXGA;

	// only in the following cases, VGA reference is used:
	// 1. Depth is running in 60 FPS
	// 2. IR stream is running in QVGA
	if ((m_Stream1Mode.GetValue() == XN_VIDEO_STREAM_DEPTH && m_DepthFPS.GetValue() == 60) ||
		(m_Stream0Mode.GetValue() == XN_VIDEO_STREAM_IR && m_IRResolution.GetValue() == XN_RESOLUTION_QVGA))
	{
		nRes = XN_RESOLUTION_VGA;
	}

	if (nRes != m_ReferenceResolution.GetValue())
	{
		nRetVal = m_ReferenceResolution.UnsafeUpdateValue(nRes);
		XN_IS_STATUS_OK(nRetVal);
	}

	return (XN_STATUS_OK);
}

XnStatus XN_CALLBACK_TYPE XnSensorFirmwareParams::SetFirmwareParamCallback(XnActualIntProperty* pSender, XnUInt64 nValue, void* pCookie)
{
	XnSensorFirmwareParams* pThis = (XnSensorFirmwareParams*)pCookie;
	return pThis->SetFirmwareParam(pSender, nValue);
}

XnStatus XN_CALLBACK_TYPE XnSensorFirmwareParams::SetFirmwareAudioParamCallback(XnActualIntProperty* pSender, XnUInt64 nValue, void* pCookie)
{
	XnSensorFirmwareParams* pThis = (XnSensorFirmwareParams*)pCookie;
	return pThis->SetFirmwareAudioParam(pSender, nValue);
}

XnStatus XN_CALLBACK_TYPE XnSensorFirmwareParams::SetImageResolutionCallback(XnActualIntProperty* /*pSender*/, XnUInt64 nValue, void* pCookie)
{
	XnSensorFirmwareParams* pThis = (XnSensorFirmwareParams*)pCookie;
	return pThis->SetImageResolution(nValue);
}

XnStatus XN_CALLBACK_TYPE XnSensorFirmwareParams::SetImageFormatCallback(XnActualIntProperty* /*pSender*/, XnUInt64 nValue, void* pCookie)
{
	XnSensorFirmwareParams* pThis = (XnSensorFirmwareParams*)pCookie;
	return pThis->SetImageFormat(nValue);
}

XnStatus XN_CALLBACK_TYPE XnSensorFirmwareParams::SetStreamModeCallback(XnActualIntProperty* pSender, XnUInt64 nValue, void* pCookie)
{
	XnSensorFirmwareParams* pThis = (XnSensorFirmwareParams*)pCookie;
	return pThis->SetStreamMode(pSender, nValue);
}

XnStatus XN_CALLBACK_TYPE XnSensorFirmwareParams::ReferenceResolutionPropertyValueChanged(const XnProperty* /*pSender*/, void* pCookie)
{
	XnSensorFirmwareParams* pThis = (XnSensorFirmwareParams*)pCookie;
	return pThis->RecalculateReferenceResolution();
}
