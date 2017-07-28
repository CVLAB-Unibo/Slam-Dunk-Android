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
#include "XnPacked12DepthProcessor.h"
#include <XnProfiling.h>
#ifdef XN_NEON
#include <arm_neon.h>
#endif

//---------------------------------------------------------------------------
// Defines
//---------------------------------------------------------------------------
/* The size of an input element in the stream. */
#define XN_INPUT_ELEMENT_SIZE 24
/* The size of an output element in the stream. */
#define XN_OUTPUT_ELEMENT_SIZE 32

//---------------------------------------------------------------------------
// Macros
//---------------------------------------------------------------------------
/* Returns a set of <count> bits. For example XN_ON_BITS(4) returns 0xF */
#define XN_ON_BITS(count)				((1 << count)-1)

/* Creates a mask of <count> bits in offset <offset> */
#define XN_CREATE_MASK(count, offset)	(XN_ON_BITS(count) << offset)

/* Takes the <count> bits in offset <offset> from <source>.
*  For example: 
*  If we want 3 bits located in offset 2 from 0xF4:
*  11110100
*     ---
*  we get 101, which is 0x5.
*  and so, XN_TAKE_BITS(0xF4,3,2) == 0x5.
*/
#define XN_TAKE_BITS(source, count, offset)		((source & XN_CREATE_MASK(count, offset)) >> offset)

//---------------------------------------------------------------------------
// Code
//---------------------------------------------------------------------------
XnPacked12DepthProcessor::XnPacked12DepthProcessor(XnSensorDepthStream* pStream, XnSensorStreamHelper* pHelper, XnFrameBufferManager* pBufferManager) :
	XnDepthProcessor(pStream, pHelper, pBufferManager)
{
}

XnStatus XnPacked12DepthProcessor::Init()
{
	XnStatus nRetVal = XN_STATUS_OK;

	nRetVal = XnDepthProcessor::Init();
	XN_IS_STATUS_OK(nRetVal);

	XN_VALIDATE_BUFFER_ALLOCATE(m_ContinuousBuffer, XN_INPUT_ELEMENT_SIZE);

	return (XN_STATUS_OK);
}

XnPacked12DepthProcessor::~XnPacked12DepthProcessor()
{
}

XnStatus XnPacked12DepthProcessor::Unpack12to16(const XnUInt8* pcInput, const XnUInt32 nInputSize, XnUInt32* pnActualRead)
{
	const XnUInt8* pOrigInput = pcInput;

	XnUInt32 nElements = nInputSize / XN_INPUT_ELEMENT_SIZE; // floored
	XnUInt32 nNeededOutput = nElements * XN_OUTPUT_ELEMENT_SIZE;

	*pnActualRead = 0;
	XnBuffer* pWriteBuffer = GetWriteBuffer();

	if (!CheckDepthBufferForOverflow(nNeededOutput))
	{
		return XN_STATUS_OUTPUT_BUFFER_OVERFLOW;
	}

	XnUInt16* pnOutput = GetDepthOutputBuffer();
	XnUInt16* pShiftOut = GetShiftsOutputBuffer();
	XnUInt16 shift[16];
#ifdef XN_NEON
	XnUInt16 depth[16];
	uint8x8x3_t inD3;
	uint8x8_t rshft4D, lshft4D;
	uint16x8_t rshft4Q, lshft4Q;
	uint16x8_t depthQ;
	uint16x8x2_t shiftQ2;
#endif

	// Convert the 11bit packed data into 16bit shorts
	for (XnUInt32 nElem = 0; nElem < nElements; ++nElem)
	{
#ifndef XN_NEON
		// input:	0,  1,2,3,  4,5,6,  7,8,9, 10,11,12, 13,14,15, 16,17,18, 19,20,21, 22,23
		//			-,---,-,-,---,-,-,---,-,-,---,--,--,---,--,--,---,--,--,---,--,--,---,--
		// bits:	8,4,4,8,8,4,4,8,8,4,4,8,8,4,4, 8, 8,4,4, 8, 8,4,4, 8, 8,4,4, 8, 8,4,4, 8
		//			---,---,---,---,---,---,---,----,----,----,----,----,----,----,----,----
		// output:	  0,  1,  2,  3,  4,  5,  6,   7,   8,   9,  10,  11,  12,  13,  14,  15

		shift[0] = (XN_TAKE_BITS(pcInput[0],8,0) << 4) | XN_TAKE_BITS(pcInput[1],4,4);
		shift[1] = (XN_TAKE_BITS(pcInput[1],4,0) << 8) | XN_TAKE_BITS(pcInput[2],8,0);
		shift[2] = (XN_TAKE_BITS(pcInput[3],8,0) << 4) | XN_TAKE_BITS(pcInput[4],4,4);
		shift[3] = (XN_TAKE_BITS(pcInput[4],4,0) << 8) | XN_TAKE_BITS(pcInput[5],8,0);
		shift[4] = (XN_TAKE_BITS(pcInput[6],8,0) << 4) | XN_TAKE_BITS(pcInput[7],4,4);
		shift[5] = (XN_TAKE_BITS(pcInput[7],4,0) << 8) | XN_TAKE_BITS(pcInput[8],8,0);
		shift[6] = (XN_TAKE_BITS(pcInput[9],8,0) << 4) | XN_TAKE_BITS(pcInput[10],4,4);
		shift[7] = (XN_TAKE_BITS(pcInput[10],4,0) << 8) | XN_TAKE_BITS(pcInput[11],8,0);
		shift[8] = (XN_TAKE_BITS(pcInput[12],8,0) << 4) | XN_TAKE_BITS(pcInput[13],4,4);
		shift[9] = (XN_TAKE_BITS(pcInput[13],4,0) << 8) | XN_TAKE_BITS(pcInput[14],8,0);
		shift[10] = (XN_TAKE_BITS(pcInput[15],8,0) << 4) | XN_TAKE_BITS(pcInput[16],4,4);
		shift[11] = (XN_TAKE_BITS(pcInput[16],4,0) << 8) | XN_TAKE_BITS(pcInput[17],8,0);
		shift[12] = (XN_TAKE_BITS(pcInput[18],8,0) << 4) | XN_TAKE_BITS(pcInput[19],4,4);
		shift[13] = (XN_TAKE_BITS(pcInput[19],4,0) << 8) | XN_TAKE_BITS(pcInput[20],8,0);
		shift[14] = (XN_TAKE_BITS(pcInput[21],8,0) << 4) | XN_TAKE_BITS(pcInput[22],4,4);
		shift[15] = (XN_TAKE_BITS(pcInput[22],4,0) << 8) | XN_TAKE_BITS(pcInput[23],8,0);

		pShiftOut[0] = (((shift[0]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[0]) : 0);
		pShiftOut[1] = (((shift[1]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[1]) : 0);
		pShiftOut[2] = (((shift[2]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[2]) : 0);
		pShiftOut[3] = (((shift[3]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[3]) : 0);
		pShiftOut[4] = (((shift[4]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[4]) : 0);
		pShiftOut[5] = (((shift[5]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[5]) : 0);
		pShiftOut[6] = (((shift[6]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[6]) : 0);
		pShiftOut[7] = (((shift[7]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[7]) : 0);
		pShiftOut[8] = (((shift[0]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[8]) : 0);
		pShiftOut[9] = (((shift[1]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[9]) : 0);
		pShiftOut[10] = (((shift[2]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[10]) : 0);
		pShiftOut[11] = (((shift[3]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[11]) : 0);
		pShiftOut[12] = (((shift[4]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[12]) : 0);
		pShiftOut[13] = (((shift[5]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[13]) : 0);
		pShiftOut[14] = (((shift[6]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[14]) : 0);
		pShiftOut[15] = (((shift[7]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[15]) : 0);

		pnOutput[0] = GetOutput(shift[0]);
		pnOutput[1] = GetOutput(shift[1]);
		pnOutput[2] = GetOutput(shift[2]);
		pnOutput[3] = GetOutput(shift[3]);
		pnOutput[4] = GetOutput(shift[4]);
		pnOutput[5] = GetOutput(shift[5]);
		pnOutput[6] = GetOutput(shift[6]);
		pnOutput[7] = GetOutput(shift[7]);
		pnOutput[8] = GetOutput(shift[8]);
		pnOutput[9] = GetOutput(shift[9]);
		pnOutput[10] = GetOutput(shift[10]);
		pnOutput[11] = GetOutput(shift[11]);
		pnOutput[12] = GetOutput(shift[12]);
		pnOutput[13] = GetOutput(shift[13]);
		pnOutput[14] = GetOutput(shift[14]);
		pnOutput[15] = GetOutput(shift[15]);

#else
		// input:	0,  1,2    (X8)
		//			-,---,-
		// bits:	8,4,4,8    (X8)
		//			---,---
		// output:	  0,  1    (X8)

		// Split 24 bytes into 3 vectors (64 bit each)
		inD3 = vld3_u8(pcInput);

		// rshft4D0 contains 4 MSB of second vector (placed at offset 0)
		rshft4D = vshr_n_u8(inD3.val[1], 4);
		// lshft4D0 contains 4 LSB of second vector (placed at offset 4)
		lshft4D = vshl_n_u8(inD3.val[1], 4);

		// Expand 64 bit vectors to 128 bit (8 values of 16 bits)
		shiftQ2.val[0] = vmovl_u8(inD3.val[0]);
		shiftQ2.val[1] = vmovl_u8(inD3.val[2]);
		rshft4Q = vmovl_u8(rshft4D);
		lshft4Q = vmovl_u8(lshft4D);

		// Even indexed shift = 8 bits from first vector + 4 MSB bits of second vector
		shiftQ2.val[0] = vshlq_n_u16(shiftQ2.val[0], 4);
		shiftQ2.val[0] = vorrq_u16(shiftQ2.val[0], rshft4Q);
		
		// Odd indexed shift = 4 LSB bits of second vector + 8 bits from third vector
		lshft4Q = vshlq_n_u16(lshft4Q, 4);
		shiftQ2.val[1] = vorrq_u16(shiftQ2.val[1], lshft4Q);
		
		// Interleave shift values to a single vector
		vst2q_u16(shift, shiftQ2);

		shift[0] = (((shift[0]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[0]) : 0);
		shift[1] = (((shift[1]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[1]) : 0);
		shift[2] = (((shift[2]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[2]) : 0);
		shift[3] = (((shift[3]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[3]) : 0);
		shift[4] = (((shift[4]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[4]) : 0);
		shift[5] = (((shift[5]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[5]) : 0);
		shift[6] = (((shift[6]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[6]) : 0);
		shift[7] = (((shift[7]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[7]) : 0);
		shift[8] = (((shift[0]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[8]) : 0);
		shift[9] = (((shift[1]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[9]) : 0);
		shift[10] = (((shift[2]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[10]) : 0);
		shift[11] = (((shift[3]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[11]) : 0);
		shift[12] = (((shift[4]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[12]) : 0);
		shift[13] = (((shift[5]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[13]) : 0);
		shift[14] = (((shift[6]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[14]) : 0);
		shift[15] = (((shift[7]) < (XN_DEVICE_SENSOR_MAX_SHIFT_VALUE-1)) ? (shift[15]) : 0);

		depth[0] = GetOutput(shift[0]);
		depth[1] = GetOutput(shift[1]);

		depth[2] = GetOutput(shift[2]);
		depth[3] = GetOutput(shift[3]);

		depth[4] = GetOutput(shift[4]);
		depth[5] = GetOutput(shift[5]);

		depth[6] = GetOutput(shift[6]);
		depth[7] = GetOutput(shift[7]);

		// Load
		depthQ = vld1q_u16(depth);
		//Store
		vst1q_u16(pnOutput, depthQ);

		// Load
		depthQ = vld1q_u16(shift);
		// Store
		vst1q_u16(pShiftOut, depthQ);

		depth[8] = GetOutput(shift[8]);
		depth[9] = GetOutput(shift[9]);

		depth[10] = GetOutput(shift[10]);
		depth[11] = GetOutput(shift[11]);

		depth[12] = GetOutput(shift[12]);
		depth[13] = GetOutput(shift[13]);

		depth[14] = GetOutput(shift[14]);
		depth[15] = GetOutput(shift[15]);

		// Load
		depthQ = vld1q_u16(depth + 8);
		// Store
		vst1q_u16(pnOutput + 8, depthQ);

		// Load
		depthQ = vld1q_u16(shift + 8);
		// Store
		vst1q_u16(pShiftOut + 8, depthQ);

#endif

		pcInput += XN_INPUT_ELEMENT_SIZE;
		pnOutput += 16;
		pShiftOut += 16;
	}


	*pnActualRead = (XnUInt32)(pcInput - pOrigInput);
	pWriteBuffer->UnsafeUpdateSize(nNeededOutput);

	return XN_STATUS_OK;
}

void XnPacked12DepthProcessor::ProcessFramePacketChunk(const XnSensorProtocolResponseHeader* /*pHeader*/, const XnUChar* pData, XnUInt32 /*nDataOffset*/, XnUInt32 nDataSize)
{
	XN_PROFILING_START_SECTION("XnPacked12DepthProcessor::ProcessFramePacketChunk")

	XnStatus nRetVal = XN_STATUS_OK;

	// check if we have data from previous packet
	if (m_ContinuousBuffer.GetSize() != 0)
	{
		// fill in to a whole element
		XnUInt32 nReadBytes = XN_MIN(nDataSize, XN_INPUT_ELEMENT_SIZE - m_ContinuousBuffer.GetSize());
		m_ContinuousBuffer.UnsafeWrite(pData, nReadBytes);
		pData += nReadBytes;
		nDataSize -= nReadBytes;

		if (m_ContinuousBuffer.GetSize() == XN_INPUT_ELEMENT_SIZE)
		{
			// process it
			XnUInt32 nActualRead = 0;
			Unpack12to16(m_ContinuousBuffer.GetData(), XN_INPUT_ELEMENT_SIZE, &nActualRead);
			m_ContinuousBuffer.Reset();
		}
	}

	// find out the number of input elements we have
	XnUInt32 nActualRead = 0;
	nRetVal = Unpack12to16(pData, nDataSize, &nActualRead);
	if (nRetVal == XN_STATUS_OK)
	{
		pData += nActualRead;
		nDataSize -= nActualRead;

		// if we have any bytes left, store them for next packet.
		if (nDataSize > 0)
		{
			// no need to check for overflow. there can not be a case in which more than XN_INPUT_ELEMENT_SIZE
			// are left.
			m_ContinuousBuffer.UnsafeWrite(pData, nDataSize);
		}
	}

	XN_PROFILING_END_SECTION
}

void XnPacked12DepthProcessor::OnStartOfFrame(const XnSensorProtocolResponseHeader* pHeader)
{
	XnDepthProcessor::OnStartOfFrame(pHeader);
	m_ContinuousBuffer.Reset();
}

void XnPacked12DepthProcessor::OnEndOfFrame(const XnSensorProtocolResponseHeader* pHeader)
{
	XnDepthProcessor::OnEndOfFrame(pHeader);
	m_ContinuousBuffer.Reset();
}
