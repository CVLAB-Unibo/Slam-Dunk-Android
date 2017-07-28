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
#ifndef __XN_SENSOR_H__
#define __XN_SENSOR_H__

//---------------------------------------------------------------------------
// Includes
//---------------------------------------------------------------------------
#include <XnDDK/XnDeviceBase.h>
#include "XnDeviceSensorIO.h"
#include "XnParams.h"
#include "XnDeviceSensor.h"
#include "XnSensorFixedParams.h"
#include "XnSensorFirmwareParams.h"
#include <XnDDK/XnDeviceStream.h>
#include "XnSensorFirmware.h"
#include "XnCmosInfo.h"
#include "IXnSensorStream.h"
#include <XnDDK/XnIntPropertySynchronizer.h>

//---------------------------------------------------------------------------
// Defines
//---------------------------------------------------------------------------
#define XN_SENSOR_PROPERTY_INSTANCE_POINTER	"InstancePointer"

//---------------------------------------------------------------------------
// XnSensor class
//---------------------------------------------------------------------------
class XnSensor : public XnDeviceBase
{
	friend class XnServerSensorInvoker;

public:
	XnSensor(XnBool bResetOnStartup = TRUE, XnBool bLeanInit = FALSE);
	~XnSensor();

	static XnStatus GetDefinition(XnDeviceDefinition* pDeviceDefinition);
	static XnStatus Enumerate(XnConnectionString* aConnectionStrings, XnUInt32* pnCount);

	virtual XnStatus InitImpl(const XnDeviceConfig* pDeviceConfig);
	virtual XnStatus Destroy();
	virtual XnStatus OpenAllStreams();
	virtual XnStatus ReadStream(XnStreamData* pStreamOutput);
	virtual XnStatus Read(XnStreamDataSet* pStreamOutputSet);
	virtual XnStatus WriteStream(XnStreamData* pStreamOutput);
	virtual XnStatus Write(XnStreamDataSet* pStreamOutputSet);
	virtual XnStatus Seek(XnUInt64 nTimestamp);
	virtual XnStatus SeekFrame(XnUInt32 nFrameID);
	virtual XnStatus LoadConfigFromFile(const XnChar* csINIFilePath, const XnChar* csSectionName);

public:
	inline XnSensorFixedParams* GetFixedParams() { return GetFirmware()->GetFixedParams(); }
	inline XnSensorFirmware* GetFirmware() { return &m_Firmware; }
	inline XnSensorFPS* GetFPSCalculator() { return &m_FPS; }

	XnStatus SetCmosConfiguration(XnCMOSType nCmos, XnResolutions nRes, XnUInt32 nFPS);

	inline XnDevicePrivateData* GetDevicePrivateData() { return &m_DevicePrivateData; }

	XnStatus ConfigPropertyFromFile(XnStringProperty* pProperty, const XnChar* csINIFilePath, const XnChar* csSectionName);
	XnStatus ConfigPropertyFromFile(XnIntProperty* pProperty, const XnChar* csINIFilePath, const XnChar* csSectionName);

	inline XnBool IsMiscSupported() const { return m_SensorIO.IsMiscEndpointSupported(); }
	inline XnBool IsLowBandwidth() const { return m_SensorIO.IsLowBandwidth(); }

	XnStatus GetBufferPool(const XnChar* strStream, XnBufferPool** ppBufferPool);
	XnStatus GetStream(const XnChar* strStream, XnDeviceStream** ppStream);

	inline XnStatus GetErrorState() { return (XnStatus)m_ErrorState.GetValue(); }
	XnStatus SetErrorState(XnStatus errorState);

	static XnStatus ResolveGlobalConfigFileName(XnChar* strConfigFile, XnUInt32 nBufSize, const XnChar* strConfigDir);
	XnStatus SetGlobalConfigFile(const XnChar* strConfigFile);
	XnStatus ConfigureModuleFromGlobalFile(const XnChar* strModule, const XnChar* strSection = NULL);

	const XnChar* GetUSBPath() { return m_USBPath.GetValue(); }
	XnBool ShouldUseHostTimestamps() { return (m_HostTimestamps.GetValue() == TRUE); }
	XnBool HasReadingStarted() { return (m_ReadData.GetValue() == TRUE); }


protected:
	virtual XnStatus CreateStreamImpl(const XnChar* strType, const XnChar* strName, const XnActualPropertiesHash* pInitialSet);

	XnStatus CreateDeviceModule(XnDeviceModuleHolder** ppModuleHolder);
	XnStatus CreateStreamModule(const XnChar* StreamType, const XnChar* StreamName, XnDeviceModuleHolder** ppStream);
	void DestroyStreamModule(XnDeviceModuleHolder* pStreamHolder);

	XnStatus WaitForPrimaryStream(XN_EVENT_HANDLE hNewDataEvent, XnStreamDataSet* pSet);

private:
	XnStatus InitSensor(const XnDeviceConfig* pDeviceConfig);
	XnStatus ValidateSensorID(XnChar* csSensorID);
	XnStatus ReadFromStreamImpl(XnDeviceStream* pStream, XnStreamData* pStreamOutput);
	XnStatus SetMirrorForModule(XnDeviceModule* pModule, XnUInt64 nValue);
	XnStatus FindSensorStream(const XnChar* StreamName, IXnSensorStream** ppStream);
	XnStatus CheckIfReadingAllowed();
	XnStatus InitReading();
	XnBool HasSynchedFrameArrived(const XnChar* strDepthStream, const XnChar* strImageStream);
	XnStatus OnFrameSyncPropertyChanged();

	static XnStatus XN_CALLBACK_TYPE GetInstanceCallback(const XnGeneralProperty* pSender, const XnGeneralBuffer& gbValue, void* pCookie);


	//---------------------------------------------------------------------------
	// Getters
	//---------------------------------------------------------------------------
	XnStatus GetFirmwareParam(XnInnerParamData* pParam);
	XnStatus GetCmosBlankingUnits(XnCmosBlankingUnits* pBlanking);
	XnStatus GetCmosBlankingTime(XnCmosBlankingTime* pBlanking);
	XnStatus GetFirmwareMode(XnParamCurrentMode* pnMode);
	XnStatus GetLastRawFrame(const XnChar* strStream, XnUChar* pBuffer, XnUInt32 nDataSize);
	XnStatus GetFixedParams(XnDynamicSizeBuffer* pBuffer);
	XnStatus GetDepthCmosRegister(XnControlProcessingData* pRegister);
	XnStatus GetImageCmosRegister(XnControlProcessingData* pRegister);
	XnStatus ReadAHB(XnAHBData* pData);


	//---------------------------------------------------------------------------
	// Setters
	//---------------------------------------------------------------------------
	XnStatus SetInterface(XnSensorUsbInterface nInterface);
	XnStatus SetHostTimestamps(XnBool bHostTimestamps);
	XnStatus SetNumberOfBuffers(XnUInt32 nCount);
	XnStatus SetReadEndpoint1(XnBool bRead);
	XnStatus SetReadEndpoint2(XnBool bRead);
	XnStatus SetReadEndpoint3(XnBool bRead);
	XnStatus SetReadData(XnBool bRead);
	XnStatus SetFirmwareParam(const XnInnerParamData* pParam);
	XnStatus SetCmosBlankingUnits(const XnCmosBlankingUnits* pBlanking);
	XnStatus SetCmosBlankingTime(const XnCmosBlankingTime* pBlanking);
	XnStatus Reset(XnParamResetType nType);
	XnStatus SetFirmwareMode(XnParamCurrentMode nMode);
	XnStatus SetDepthCmosRegister(const XnControlProcessingData* pRegister);
	XnStatus SetImageCmosRegister(const XnControlProcessingData* pRegister);
	XnStatus WriteAHB(const XnAHBData* pData);


	//---------------------------------------------------------------------------
	// Callbacks
	//---------------------------------------------------------------------------
	static XnStatus XN_CALLBACK_TYPE SetInterfaceCallback(XnActualIntProperty* pSender, XnUInt64 nValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE SetHostTimestampsCallback(XnActualIntProperty* pSender, XnUInt64 nValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE SetNumberOfBuffersCallback(XnActualIntProperty* pSender, XnUInt64 nValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE SetReadEndpoint1Callback(XnActualIntProperty* pSender, XnUInt64 nValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE SetReadEndpoint2Callback(XnActualIntProperty* pSender, XnUInt64 nValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE SetReadEndpoint3Callback(XnActualIntProperty* pSender, XnUInt64 nValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE SetReadDataCallback(XnActualIntProperty* pSender, XnUInt64 nValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE SetFirmwareParamCallback(XnGeneralProperty* pSender, const XnGeneralBuffer& gbValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE SetCmosBlankingUnitsCallback(XnGeneralProperty* pSender, const XnGeneralBuffer& gbValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE SetCmosBlankingTimeCallback(XnGeneralProperty* pSender, const XnGeneralBuffer& gbValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE ResetCallback(XnIntProperty* pSender, XnUInt64 nValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE SetFirmwareModeCallback(XnIntProperty* pSender, XnUInt64 nValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE GetFixedParamsCallback(const XnGeneralProperty* pSender, const XnGeneralBuffer& gbValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE FrameSyncPropertyChangedCallback(const XnProperty* pSender, void* pCookie);
	static XnBool XN_CALLBACK_TYPE HasSynchedFrameArrived(void* pCookie);
	static XnBool XN_CALLBACK_TYPE USBEventCallback(XnUSBEventType USBEventType, XnChar* cpDevPath, void* pCallbackData);
	static XnStatus XN_CALLBACK_TYPE GetFirmwareParamCallback(const XnGeneralProperty* pSender, const XnGeneralBuffer& gbValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE GetCmosBlankingUnitsCallback(const XnGeneralProperty* pSender, const XnGeneralBuffer& gbValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE GetCmosBlankingTimeCallback(const XnGeneralProperty* pSender, const XnGeneralBuffer& gbValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE GetFirmwareModeCallback(const XnIntProperty* pSender, XnUInt64* pnValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE GetAudioSupportedCallback(const XnIntProperty* pSender, XnUInt64* pnValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE GetImageSupportedCallback(const XnIntProperty* pSender, XnUInt64* pnValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE SetDepthCmosRegisterCallback(XnGeneralProperty* pSender, const XnGeneralBuffer& gbValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE SetImageCmosRegisterCallback(XnGeneralProperty* pSender, const XnGeneralBuffer& gbValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE GetDepthCmosRegisterCallback(const XnGeneralProperty* pSender, const XnGeneralBuffer& gbValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE GetImageCmosRegisterCallback(const XnGeneralProperty* pSender, const XnGeneralBuffer& gbValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE ReadAHBCallback(const XnGeneralProperty* pSender, const XnGeneralBuffer& gbValue, void* pCookie);
	static XnStatus XN_CALLBACK_TYPE WriteAHBCallback(XnGeneralProperty* pSender, const XnGeneralBuffer& gbValue, void* pCookie);


	//---------------------------------------------------------------------------
	// Members
	//---------------------------------------------------------------------------
	XnActualIntProperty m_ErrorState;
	XnActualIntProperty m_ResetSensorOnStartup;
	XnActualIntProperty m_LeanInit;
	XnActualIntProperty m_Interface;
	XnActualIntProperty m_ReadFromEP1;
	XnActualIntProperty m_ReadFromEP2;
	XnActualIntProperty m_ReadFromEP3;
	XnActualIntProperty m_ReadData;
	XnActualIntProperty m_FrameSync;
	XnActualIntProperty m_CloseStreamsOnShutdown;
	XnActualIntProperty m_HostTimestamps;
	XnGeneralProperty m_FirmwareParam;
	XnGeneralProperty m_CmosBlankingUnits;
	XnGeneralProperty m_CmosBlankingTime;
	XnIntProperty m_Reset;
	XnIntProperty m_FirmwareMode;
	XnVersions m_VersionData;
	XnActualGeneralProperty m_Version;
	XnGeneralProperty m_FixedParam;
	XnGeneralProperty m_InstancePointer;
	XnActualStringProperty m_ID;
	XnActualStringProperty m_USBPath;
	XnActualStringProperty m_DeviceName;
	XnActualStringProperty m_VendorSpecificData;
	XnActualStringProperty m_PlatformString;
	XnIntProperty m_AudioSupported;
	XnIntProperty m_ImageSupported;
	XnGeneralProperty m_ImageControl;
	XnGeneralProperty m_DepthControl;
	XnGeneralProperty m_AHB;


	XnSensorFirmware m_Firmware;
	XnDevicePrivateData m_DevicePrivateData;
	XnSensorFPS m_FPS;
	XnCmosInfo m_CmosInfo;
	XnSensorIO m_SensorIO;

	XnSensorObjects m_Objects;


	XnDumpFile* m_FrameSyncDump;
	XnBool m_bInitialized;

	XnIntPropertySynchronizer m_PropSynchronizer;

	XnChar m_strGlobalConfigFile[XN_FILE_MAX_PATH];
};

#endif //__XN_SENSOR_H__