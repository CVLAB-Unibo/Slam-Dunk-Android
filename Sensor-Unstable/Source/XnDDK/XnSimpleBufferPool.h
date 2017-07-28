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
#ifndef __XN_SIMPLE_BUFFER_POOL_H__
#define __XN_SIMPLE_BUFFER_POOL_H__

//---------------------------------------------------------------------------
// Includes
//---------------------------------------------------------------------------
#include "XnBufferPool.h"

//---------------------------------------------------------------------------
// Types
//---------------------------------------------------------------------------
class XnSimpleBufferPool : public XnBufferPool
{
public:
	XnSimpleBufferPool(XnUInt32 nBufferCount);
	~XnSimpleBufferPool();

protected:
	virtual XnStatus AllocateBuffers(XnUInt32 nSize);
	virtual void DestroyBuffer(void* pBuffer);

private:
	XnUInt32 m_nBufferCount;
};

#endif // __XN_SIMPLE_BUFFER_POOL_H__