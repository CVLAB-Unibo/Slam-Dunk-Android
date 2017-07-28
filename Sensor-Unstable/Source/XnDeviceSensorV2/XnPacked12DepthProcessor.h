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
#ifndef __XN_PACKED_12_DEPTH_PROCESSOR_H__
#define __XN_PACKED_12_DEPTH_PROCESSOR_H__

//---------------------------------------------------------------------------
// Includes
//---------------------------------------------------------------------------
#include "XnDepthProcessor.h"

//---------------------------------------------------------------------------
// Code
//---------------------------------------------------------------------------

class XnPacked12DepthProcessor : public XnDepthProcessor
{
public:
	XnPacked12DepthProcessor(XnSensorDepthStream* pStream, XnSensorStreamHelper* pHelper, XnFrameBufferManager* pBufferManager);
	virtual ~XnPacked12DepthProcessor();

	XnStatus Init();

protected:
	//---------------------------------------------------------------------------
	// Overridden Functions
	//---------------------------------------------------------------------------
	virtual void ProcessFramePacketChunk(const XnSensorProtocolResponseHeader* pHeader, const XnUChar* pData, XnUInt32 nDataOffset, XnUInt32 nDataSize);
	virtual	void OnStartOfFrame(const XnSensorProtocolResponseHeader* pHeader);
	virtual	void OnEndOfFrame(const XnSensorProtocolResponseHeader* pHeader);

	//---------------------------------------------------------------------------
	// Internal Functions
	//---------------------------------------------------------------------------
	XnStatus Unpack12to16(const XnUInt8* pcInput, const XnUInt32 nInputSize, XnUInt32* pnActualRead);

	//---------------------------------------------------------------------------
	// Class Members
	//---------------------------------------------------------------------------
private:
	/* A buffer used for storing some left-over bytes for the next packet. */
	XnBuffer m_ContinuousBuffer;
};

#endif //__XN_PACKED_12_DEPTH_PROCESSOR_H__
