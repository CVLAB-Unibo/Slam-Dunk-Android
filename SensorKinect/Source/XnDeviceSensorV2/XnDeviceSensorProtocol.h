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
#ifndef _XN_DEVICESENSORPROTOCOL_H_
#define _XN_DEVICESENSORPROTOCOL_H_

//---------------------------------------------------------------------------
// Includes
//---------------------------------------------------------------------------
#include "XnDeviceSensor.h"
#include "XnHostProtocol.h"

//---------------------------------------------------------------------------
// Defines
//---------------------------------------------------------------------------
#define XN_SENSOR_PROTOCOL_SENSOR_CLOCK_LENGTH	2
#define XN_SENSOR_PROTOCOL_SENSOR_VER_LENGTH 1

#define XN_SENSOR_PROTOCOL_RESPONSE_DEPTH_START						0x7100
#define XN_SENSOR_PROTOCOL_RESPONSE_DEPTH_BUFFER					0x7200
#define XN_SENSOR_PROTOCOL_RESPONSE_DEPTH_END						0x7500

#define XN_SENSOR_PROTOCOL_RESPONSE_IMAGE_START						0x8100
#define XN_SENSOR_PROTOCOL_RESPONSE_IMAGE_BUFFER					0x8200
#define XN_SENSOR_PROTOCOL_RESPONSE_IMAGE_END						0x8500

#define XN_SENSOR_PROTOCOL_RESPONSE_AUDIO_BUFFER					0x9200

#define XN_SENSOR_PROTOCOL_RESPONSE_GMC								0xa200

#define XN_SENSOR_PROTOCOL_RESPONSE_GMC_DEBUG						0xb200
#define XN_SENSOR_PROTOCOL_RESPONSE_GMC_DEBUG_END					0xb500

#define XN_SENSOR_PROTOCOL_RESPONSE_WAVELENGTH_CORRECTION_DEBUG		0xc200

#define XN_SENSOR_PROTOCOL_RESPONSE_TEC_DEBUG						0xd200

#define XN_SENSOR_PROTOCOL_RESPONSE_PROJECTOR_FAULT_EVENT			0xdead

#define XN_SENSOR_PROTOCOL_RESPONSE_OVERHEAT						0xf31f

#define XN_SENSOR_PROTOCOL_MAX_BUFFER_SIZE 1024*1024
#define XN_SENSOR_PROTOCOL_USB_BUFFER_SIZE 4*1024*1024
#define XN_SENSOR_PROTOCOL_USB_MAX_ZERO_READ_COUNTER 5
#define XN_SENSOR_PROTOCOL_READ_SLEEP 100
#define XN_SENSOR_PROTOCOL_READ_MAX_TRIES 30

#define XN_SENSOR_PROTOCOL_MAX_RECONFIG_TRIES 10

#define XN_SENSOR_PROTOCOL_START_CAPTURE_TRIES 5
#define XN_SENSOR_PROTOCOL_START_CAPTURE_SLEEP 1000

#define XN_SENSOR_PROTOCOL_GMC_MAX_POINTS_IN_PACKET 100

/** the number of points to accumulate before processing takes place. */
#define XN_GMC_MIN_COUNT_FOR_RUNNING	1000

//---------------------------------------------------------------------------
// Structures
//---------------------------------------------------------------------------
#pragma pack (push, 1)

typedef struct XnSensorProtocolResponseHeader
{
	XnUInt16 nMagic;
	XnUInt16 nType;
	// --avin mod--
	XnUInt8  nPacketID;
	XnUInt8  nUnknown;
	XnUInt16 nBufSize;
	XnUInt32 nTimeStamp;
} XnSensorProtocolResponseHeader;
#pragma pack (pop) // Undo the pack change...

typedef enum
{
	XN_WAITING_FOR_CONFIGURATION,
	XN_IGNORING_GARBAGE,
	XN_LOOKING_FOR_MAGIC,
	XN_HALF_MAGIC,
	XN_PACKET_HEADER,
	XN_PACKET_DATA
} XnMiniPacketState;

typedef struct XnSpecificUsbDeviceState
{
	XnMiniPacketState State;
	XnSensorProtocolResponseHeader CurrHeader;
	XnUInt32 nMissingBytesInState;
} XnSpecificUsbDeviceState;

typedef struct XnSpecificUsbDevice
{
	XnDevicePrivateData* pDevicePrivateData;
	XnUsbConnection* pUsbConnection;
	XnUInt32 nIgnoreBytes;
	XnUInt32 nChunkReadBytes;
	XnSpecificUsbDeviceState CurrState;
	XnUInt32 nTimeout;
} XnSpecificUsbDevice;


//---------------------------------------------------------------------------
// Functions Declaration
//---------------------------------------------------------------------------
XnBool XN_CALLBACK_TYPE XnDeviceSensorProtocolUsbEpCb(XnUChar* pBuffer, XnUInt32 nBufferSize, void* pCallbackData);

XnStatus XnCalculateExpectedImageSize(XnDevicePrivateData* pDevicePrivateData, XnUInt32* pnExpectedSize);
void XnProcessUncompressedDepthPacket(XnSensorProtocolResponseHeader* pCurrHeader, XnUChar* pData, XnUInt32 nDataSize, XnBool bEOP, XnSpecificUsbDevice* pSpecificDevice);
XnStatus XnDeviceSensorProtocolUpdateImageProcessor(XnDevicePrivateData* pDevicePrivateData);



#endif //_XN_DEVICESENSORPROTOCOL_H_
