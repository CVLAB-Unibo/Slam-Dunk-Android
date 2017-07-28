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
#include "XnBayerImageProcessor.h"
#include "Uncomp.h"
#include "Bayer.h"
#include <XnProfiling.h>

//---------------------------------------------------------------------------
// Code
//---------------------------------------------------------------------------

XnBayerImageProcessor::XnBayerImageProcessor(XnSensorImageStream* pStream, XnSensorStreamHelper* pHelper, XnFrameBufferManager* pBufferManager) :
	XnImageProcessor(pStream, pHelper, pBufferManager)
{
}

XnBayerImageProcessor::~XnBayerImageProcessor()
{
}

XnStatus XnBayerImageProcessor::Init()
{
	XnStatus nRetVal = XN_STATUS_OK;

	nRetVal = XnImageProcessor::Init();
	XN_IS_STATUS_OK(nRetVal);

	XN_VALIDATE_BUFFER_ALLOCATE(m_ContinuousBuffer, GetExpectedOutputSize());

	switch (GetStream()->GetOutputFormat())
	{
	case XN_OUTPUT_FORMAT_GRAYSCALE8:
		break;
	case XN_OUTPUT_FORMAT_RGB24:
		XN_VALIDATE_BUFFER_ALLOCATE(m_UncompressedBayerBuffer, GetExpectedOutputSize());
		break;
	default:
		XN_LOG_WARNING_RETURN(XN_STATUS_ERROR, XN_MASK_SENSOR_PROTOCOL_IMAGE, "Unsupported image output format: %d", GetStream()->GetOutputFormat());
	}

	return (XN_STATUS_OK);
}

void XnBayerImageProcessor::ProcessFramePacketChunk(const XnSensorProtocolResponseHeader* pHeader, const XnUChar* pData, XnUInt32 nDataOffset, XnUInt32 nDataSize)
{
	XN_PROFILING_START_SECTION("XnBayerImageProcessor::ProcessFramePacketChunk")

	// if output format is Gray8, we can write directly to output buffer. otherwise, we need
	// to write to a temp buffer.
	XnBuffer* pWriteBuffer = (GetStream()->GetOutputFormat() == XN_OUTPUT_FORMAT_GRAYSCALE8) ? GetWriteBuffer() : &m_UncompressedBayerBuffer;

	const XnUChar* pBuf = NULL;
	XnUInt32 nBufSize = 0;

	// check if we have bytes stored from previous calls
	if (m_ContinuousBuffer.GetSize() > 0)
	{
		// we have no choice. We need to append current buffer to previous bytes
		if (m_ContinuousBuffer.GetFreeSpaceInBuffer() < nDataSize)
		{
			xnLogWarning(XN_MASK_SENSOR_PROTOCOL_DEPTH, "Bad overflow image! %d", m_ContinuousBuffer.GetSize());
			FrameIsCorrupted();
		}
		else
		{
			m_ContinuousBuffer.UnsafeWrite(pData, nDataSize);
		}

		pBuf = m_ContinuousBuffer.GetData();
		nBufSize = m_ContinuousBuffer.GetSize();
	}
	else
	{
		// we can process the data directly
		pBuf = pData;
		nBufSize = nDataSize;
	}

	XnUInt32 nOutputSize = pWriteBuffer->GetFreeSpaceInBuffer();
	XnUInt32 nWrittenOutput = nOutputSize;
	XnUInt32 nActualRead = 0;
	XnBool bLastPart = pHeader->nType == XN_SENSOR_PROTOCOL_RESPONSE_IMAGE_END && (nDataOffset + nDataSize) == pHeader->nBufSize;
	XnStatus nRetVal = XnStreamUncompressImageNew(pBuf, nBufSize, pWriteBuffer->GetUnsafeWritePointer(), 
		&nWrittenOutput, (XnUInt16)GetActualXRes(), &nActualRead, bLastPart);

	if (nRetVal != XN_STATUS_OK)
	{
		xnLogWarning(XN_MASK_SENSOR_PROTOCOL_IMAGE, "Image decompression failed: %s (%d of %d, requested %d, last %d)", xnGetStatusString(nRetVal), nWrittenOutput, nBufSize, nOutputSize, bLastPart);
		FrameIsCorrupted();
	}

	pWriteBuffer->UnsafeUpdateSize(nWrittenOutput);

	nBufSize -= nActualRead;
	m_ContinuousBuffer.Reset();

	// if we have any bytes left, keep them for next time
	if (nBufSize > 0)
	{
		pBuf += nActualRead;
		m_ContinuousBuffer.UnsafeWrite(pBuf, nBufSize);
	}

	XN_PROFILING_END_SECTION
}

void XnBayerImageProcessor::OnStartOfFrame(const XnSensorProtocolResponseHeader* pHeader)
{
	XnImageProcessor::OnStartOfFrame(pHeader);
	m_ContinuousBuffer.Reset();
}

void XnBayerImageProcessor::OnEndOfFrame(const XnSensorProtocolResponseHeader* pHeader)
{
	XN_PROFILING_START_SECTION("XnBayerImageProcessor::OnEndOfFrame")

	// if data was written to temp buffer, convert it now
	switch (GetStream()->GetOutputFormat())
	{
	case XN_OUTPUT_FORMAT_GRAYSCALE8:
		break;
	case XN_OUTPUT_FORMAT_RGB24:
		{
			Bayer2RGB888(m_UncompressedBayerBuffer.GetData(), GetWriteBuffer()->GetUnsafeWritePointer(), GetActualXRes(), GetActualYRes(), 1, 0);
			GetWriteBuffer()->UnsafeUpdateSize(GetActualXRes()*GetActualYRes()*3);
			m_UncompressedBayerBuffer.Reset();
		}
		break;
	}

	XnImageProcessor::OnEndOfFrame(pHeader);
	m_ContinuousBuffer.Reset();

	XN_PROFILING_END_SECTION
}
