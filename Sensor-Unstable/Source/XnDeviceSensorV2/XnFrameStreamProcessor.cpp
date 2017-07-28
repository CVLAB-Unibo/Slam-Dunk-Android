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
#include "XnFrameStreamProcessor.h"
#include "XnSensor.h"
#include <XnProfiling.h>

//---------------------------------------------------------------------------
// Code
//---------------------------------------------------------------------------
XnFrameStreamProcessor::XnFrameStreamProcessor(XnFrameStream* pStream, XnSensorStreamHelper* pHelper, XnFrameBufferManager* pBufferManager, XnUInt16 nTypeSOF, XnUInt16 nTypeEOF) :
	XnStreamProcessor(pStream, pHelper),
	m_nTypeSOF(nTypeSOF),
	m_nTypeEOF(nTypeEOF),
	m_pTripleBuffer(pBufferManager),
	m_InDump(NULL),
	m_InternalDump(NULL),
	m_bFrameCorrupted(FALSE),
	m_bAllowDoubleSOF(FALSE),
	m_nLastSOFPacketID(0),
	m_nFirstPacketTimestamp(0)
{
	sprintf(m_csInDumpMask, "%sIn", pStream->GetType());
	sprintf(m_csInternalDumpMask, "Internal%s", pStream->GetType());
	m_InDump = xnDumpFileOpen(m_csInDumpMask, "%s_0.raw", m_csInDumpMask);
	m_InternalDump = xnDumpFileOpen(m_csInternalDumpMask, "%s_0.raw", m_csInternalDumpMask);
}

XnFrameStreamProcessor::~XnFrameStreamProcessor()
{
}

void XnFrameStreamProcessor::ProcessPacketChunk(const XnSensorProtocolResponseHeader* pHeader, const XnUChar* pData, XnUInt32 nDataOffset, XnUInt32 nDataSize)
{
	XN_PROFILING_START_SECTION("XnFrameStreamProcessor::ProcessPacketChunk");

	// if first data from SOF packet
	if (pHeader->nType == m_nTypeSOF && nDataOffset == 0)
	{
		if (!m_bAllowDoubleSOF || pHeader->nPacketID != (m_nLastSOFPacketID + 1))
		{
			m_nLastSOFPacketID = pHeader->nPacketID;
			OnStartOfFrame(pHeader);
		}
	}

	if (!m_bFrameCorrupted)
	{
		xnDumpFileWriteBuffer(m_InDump, pData, nDataSize);
		ProcessFramePacketChunk(pHeader, pData, nDataOffset, nDataSize);
	}

	// if last data from EOF packet
	if (pHeader->nType == m_nTypeEOF && (nDataOffset + nDataSize) == pHeader->nBufSize)
	{
		OnEndOfFrame(pHeader);
	}

	XN_PROFILING_END_SECTION
}

void XnFrameStreamProcessor::OnPacketLost()
{
	FrameIsCorrupted();
}

void XnFrameStreamProcessor::OnStartOfFrame(const XnSensorProtocolResponseHeader* /*pHeader*/)
{
	m_bFrameCorrupted = FALSE;
	m_pTripleBuffer->GetWriteBuffer()->Reset();
	if (m_pDevicePrivateData->pSensor->ShouldUseHostTimestamps())
	{
		m_nFirstPacketTimestamp = GetHostTimestamp();
	}
}

void XnFrameStreamProcessor::OnEndOfFrame(const XnSensorProtocolResponseHeader* pHeader)
{
	// write dump
	XnBuffer* pCurWriteBuffer = m_pTripleBuffer->GetWriteBuffer();
	xnDumpFileWriteBuffer(m_InternalDump, pCurWriteBuffer->GetData(), pCurWriteBuffer->GetSize());
	xnDumpFileClose(m_InternalDump);
	xnDumpFileClose(m_InDump);

	if (!m_bFrameCorrupted)
	{
		// mark the buffer as stable
		XnUInt64 nTimestamp;
		if (m_pDevicePrivateData->pSensor->ShouldUseHostTimestamps())
		{
			// use the host timestamp of the first packet
			nTimestamp = m_nFirstPacketTimestamp;
		}
		else
		{
			// use timestamp in last packet
			nTimestamp = CreateTimestampFromDevice(pHeader->nTimeStamp);
		}
		
		XnUInt32 nFrameID;
		m_pTripleBuffer->MarkWriteBufferAsStable(nTimestamp, &nFrameID);

		// let inheriting classes do their stuff
		OnFrameReady(nFrameID, nTimestamp);
	}
	else
	{
		// restart
		m_pTripleBuffer->GetWriteBuffer()->Reset();
	}

	// log bandwidth
	XnUInt64 nSysTime;
	xnOSGetTimeStamp(&nSysTime);
	xnDumpFileWriteString(m_pDevicePrivateData->BandwidthDump, "%llu,%s,%d,%d\n", 
		nSysTime, m_csName, GetCurrentFrameID(), m_nBytesReceived);

	// re-init dumps
	m_InDump = xnDumpFileOpen(m_csInDumpMask, "%s_%d.raw", m_csInDumpMask, GetCurrentFrameID());
	m_InternalDump = xnDumpFileOpen(m_csInternalDumpMask, "%s_%d.raw", m_csInternalDumpMask, GetCurrentFrameID());
	m_nBytesReceived = 0;
}

void XnFrameStreamProcessor::FrameIsCorrupted()
{
	if (!m_bFrameCorrupted)
	{
		xnLogWarning(XN_MASK_SENSOR_PROTOCOL, "%s frame is corrupt!", m_csName);
		m_bFrameCorrupted = TRUE;
	}
}

void XnFrameStreamProcessor::WriteBufferOverflowed()
{
	XnBuffer* pBuffer = GetWriteBuffer();
	xnLogWarning(XN_MASK_SENSOR_PROTOCOL, "%s Frame Buffer overflow! current size: %d", m_csName, pBuffer->GetSize());
	FrameIsCorrupted();
}
