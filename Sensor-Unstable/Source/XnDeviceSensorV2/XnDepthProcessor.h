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
#ifndef __XN_DEPTH_PROCESSOR_H__
#define __XN_DEPTH_PROCESSOR_H__

//---------------------------------------------------------------------------
// Includes
//---------------------------------------------------------------------------
#include "XnFrameStreamProcessor.h"
#include "XnSensorDepthStream.h"

//---------------------------------------------------------------------------
// Compilation Checks
//---------------------------------------------------------------------------

// Optimization: in order to save branches in the code itself, we create a shift-to-depth
// map which will actually translate shift-to-shift. This optimization relies on the 
// fact that both shifts and depths are 16-bit long. If this is not the case, 
// this optimization should be re-written.
// Then, any processor can always go through this LUT, no matter what the output format is.
#if (XnDepthPixel != XnUInt16)
	#error "Depth and Shift do not have the same size. Need to reconsider optimization!"
#endif

//---------------------------------------------------------------------------
// Code
//---------------------------------------------------------------------------
class XnDepthProcessor : public XnFrameStreamProcessor
{
public:
	XnDepthProcessor(XnSensorDepthStream* pStream, XnSensorStreamHelper* pHelper, XnFrameBufferManager* pBufferManager);
	virtual ~XnDepthProcessor();

	XnStatus Init();

protected:
	//---------------------------------------------------------------------------
	// Overridden Functions
	//---------------------------------------------------------------------------
	virtual void OnStartOfFrame(const XnSensorProtocolResponseHeader* pHeader);
	virtual void OnEndOfFrame(const XnSensorProtocolResponseHeader* pHeader);
	virtual void OnFrameReady(XnUInt32 nFrameID, XnUInt64 nFrameTS);

	//---------------------------------------------------------------------------
	// Helper Functions
	//---------------------------------------------------------------------------
	inline XnSensorDepthStream* GetStream()
	{
		return (XnSensorDepthStream*)XnFrameStreamProcessor::GetStream();
	}

	inline XnDepthPixel GetOutput(XnUInt16 nShift)
	{
		return m_pShiftToDepthTable[nShift];
	}

	inline XnUInt32 GetExpectedSize()
	{
		return m_nExpectedFrameSize;
	}

	inline XnDepthPixel* GetDepthOutputBuffer()
	{
		return (XnDepthPixel*)GetWriteBuffer()->GetUnsafeWritePointer();
	}

	inline XnUInt16* GetShiftsOutputBuffer()
	{
		return (XnUInt16*)(GetWriteBuffer()->GetUnsafeWritePointer() + GetExpectedSize());
	}

	inline XnBool CheckDepthBufferForOverflow(XnUInt32 nWriteSize)
	{
		// Check there is enough room for current depth pixels + the entire shift map
		return CheckWriteBufferForOverflow(nWriteSize + GetExpectedSize());
	}

	inline XnUInt32 GetFreeSpaceInDepthBuffer()
	{
		return GetWriteBuffer()->GetFreeSpaceInBuffer() - GetExpectedSize();
	}

private:
	void PadPixels(XnUInt32 nPixels);
	XnUInt32 CalculateExpectedSize();

	XnUInt32 m_nPaddingPixelsOnEnd;
	XnUInt32 m_nExpectedFrameSize;
	XnBool m_bShiftToDepthAllocated;
	XnDepthPixel* m_pShiftToDepthTable;
};

#endif //__XN_DEPTH_PROCESSOR_H__
