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
#ifndef __XN_STRING_PROPERTY_H__
#define __XN_STRING_PROPERTY_H__

//---------------------------------------------------------------------------
// Includes
//---------------------------------------------------------------------------
#include <XnDDK/XnProperty.h>

//---------------------------------------------------------------------------
// Class
//---------------------------------------------------------------------------

/**
* A property of type general.
*/
class XN_DDK_CPP_API XnStringProperty : public XnProperty
{
public:
	XnStringProperty(const XnChar* strName, XnChar* pValueHolder, const XnChar* strModule = "");

	typedef XnStatus (XN_CALLBACK_TYPE* SetFuncPtr)(XnStringProperty* pSender, const XnChar* strValue, void* pCookie);
	typedef XnStatus (XN_CALLBACK_TYPE* GetFuncPtr)(const XnStringProperty* pSender, XnChar* csValue, void* pCookie);

	inline XnStatus SetValue(const XnChar* strValue)
	{
		XN_VALIDATE_INPUT_PTR(strValue);
		return XnProperty::SetValue(strValue);
	}

	inline XnStatus GetValue(XnChar* csValue) const
	{
		XN_VALIDATE_INPUT_PTR(csValue);
		return XnProperty::GetValue(csValue);
	}

	inline XnStatus UnsafeUpdateValue(const XnChar* strValue)
	{
		XN_VALIDATE_INPUT_PTR(strValue);
		return XnProperty::UnsafeUpdateValue(strValue);
	}

	inline void UpdateSetCallback(SetFuncPtr pFunc, void* pCookie)
	{
		XnProperty::UpdateSetCallback((XnProperty::SetFuncPtr)pFunc, pCookie);
	}

	inline void UpdateGetCallback(GetFuncPtr pFunc, void* pCookie)
	{
		XnProperty::UpdateGetCallback((XnProperty::GetFuncPtr)pFunc, pCookie);
	}

	XnStatus ReadValueFromFile(const XnChar* csINIFile, const XnChar* csSection);

	XnStatus AddToPropertySet(XnPropertySet* pSet);

protected:
	//---------------------------------------------------------------------------
	// Overridden Methods
	//---------------------------------------------------------------------------
	virtual XnStatus CopyValueImpl(void* pDest, const void* pSource) const;
	virtual XnBool IsEqual(const void* pValue1, const void* pValue2) const;
	virtual XnStatus CallSetCallback(XnProperty::SetFuncPtr pFunc, const void* pValue, void* pCookie);
	virtual XnStatus CallGetCallback(XnProperty::GetFuncPtr pFunc, void* pValue, void* pCookie) const;
	virtual XnBool ConvertValueToString(XnChar* csValue, const void* pValue) const;
};

#endif //__XN_STRING_PROPERTY_H__