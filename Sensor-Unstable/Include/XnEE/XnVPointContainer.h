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
#ifndef _XNV_POINT_CONTAINER_H_
#define _XNV_POINT_CONTAINER_H_

//---------------------------------------------------------------------------
// Includes
//---------------------------------------------------------------------------

#include "XnVPoint.h"
#include "XnVContainer.h"

//---------------------------------------------------------------------------
// Types
//---------------------------------------------------------------------------

/**
 * This is a specific XnVContainer, for holding XnVPoint instances.
 */

class XN_EE_CORE_API XnVPointContainer : public XnVContainer
{
public:
	XnVPointContainer(XnUInt32 nCapacity) :
	  XnVContainer(nCapacity, sizeof(XnVPoint))
	  {}

	inline XnVPoint& operator[](XnInt32 nIndex)
	{
		return ((XnVPoint*)Data())[nIndex];
	}
	inline const XnVPoint& operator[](XnInt32 nIndex) const
	{
		return ((XnVPoint*)Data())[nIndex];
	}

	inline void Add(const XnVPoint& ptPoint);
protected:
};

#endif //_XNV_POINT_CONTAINER_H_

