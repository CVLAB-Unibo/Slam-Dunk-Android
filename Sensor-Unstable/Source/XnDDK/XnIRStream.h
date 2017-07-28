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
#ifndef __XN_IR_STREAM_H__
#define __XN_IR_STREAM_H__

//---------------------------------------------------------------------------
// Includes
//---------------------------------------------------------------------------
#include <XnDDK/XnPixelStream.h>

//---------------------------------------------------------------------------
// Types
//---------------------------------------------------------------------------

/** Represents a base class for an image stream. */
class XN_DDK_CPP_API XnIRStream : public XnPixelStream
{
public:
	XnIRStream(const XnChar* csName, XnBool bAllowCustomResolutions);
};

#endif //__XN_IR_STREAM_H__