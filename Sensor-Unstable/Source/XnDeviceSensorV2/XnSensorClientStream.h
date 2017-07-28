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
#ifndef __XN_SENSOR_CLIENT_STREAM_H__
#define __XN_SENSOR_CLIENT_STREAM_H__

//---------------------------------------------------------------------------
// Includes
//---------------------------------------------------------------------------
#include <XnDDK/XnStreamReaderStream.h>
#include "XnDeviceSensor.h"

class XnSensorClient;

//---------------------------------------------------------------------------
// Client Stream
//---------------------------------------------------------------------------
class XnSensorClientStream : public XnStreamReaderStream
{
public:
	XnSensorClientStream(XnSensorClient* pClient, const XnChar* strType, const XnChar* strName);
	~XnSensorClientStream();

	XnStatus Free();
	virtual XnStatus OpenSharedMemory();
	virtual XnStatus Open();
	virtual XnStatus Close();

protected:
	XnSensorClient* m_pClient;
	XN_SHARED_MEMORY_HANDLE m_hSharedMemory;
	XnUChar* m_pSharedMemory;
};

//---------------------------------------------------------------------------
// Client Frame Stream
//---------------------------------------------------------------------------
class XnSensorClientFrameStream : public XnSensorClientStream
{
public:
	XnSensorClientFrameStream(XnSensorClient* pClient, const XnChar* strType, const XnChar* strName);
	~XnSensorClientFrameStream();

	XnStatus CreateStreamData(XnStreamData** ppStreamData);

protected:
	XnStatus ReadImpl(XnStreamData* pStreamOutput);

private:
	XnUInt32 m_nFrameID;
};

//---------------------------------------------------------------------------
// Client Depth Stream
//---------------------------------------------------------------------------
class XnSensorClientDepthStream : public XnSensorClientFrameStream
{
public:
	XnSensorClientDepthStream(XnSensorClient* pClient, const XnChar* strType, const XnChar* strName);
	~XnSensorClientDepthStream();

protected:
	XnStatus ReadImpl(XnStreamData* pStreamOutput);
	virtual XnStatus GetProperty(const XnChar* strName, const XnGeneralBuffer& gbValue) const;

private:
	XnUInt16* m_pLastFrameShiftsMapOffset;
};

//---------------------------------------------------------------------------
// Client Audio Stream
//---------------------------------------------------------------------------
class XnSensorClientAudioStream : public XnSensorClientStream
{
public:
	XnSensorClientAudioStream(XnSensorClient* pClient, const XnChar* strType, const XnChar* strName);
	~XnSensorClientAudioStream();

	XnStatus Init();
	XnStatus Free();

	XnStatus OpenSharedMemory();

	virtual void NewDataAvailable(XnUInt64 nTimestamp, XnUInt32 nFrameID);

protected:
	XnStatus ReadImpl(XnStreamData* pStreamOutput);

private:
	XnUInt32 m_nFrameID;
	XnAudioSharedBuffer* m_pHeader;
	XnUChar* m_pBuffer;
	XnUInt32 m_nLastReadIndex;
	XnUInt64* m_pTimestamps;
	XN_CRITICAL_SECTION_HANDLE m_hLock;
};

#endif // __XN_SENSOR_CLIENT_STREAM_H__