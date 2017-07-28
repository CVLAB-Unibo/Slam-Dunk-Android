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
#ifndef XN_PARAMS_H
#define XN_PARAMS_H

typedef enum 
{
	//General,
	PARAM_GENERAL_CURRENT_MODE = 0,
	PARAM_GENERAL_FRAME_SYNC = 1,
	PARAM_GENERAL_REGISTRATION_ENABLE = 2,
	PARAM_GENERAL_STREAM_PRIORITY = 3,
	PARAM_GENERAL_TRIGGER_ACTION = 4,
	PARAM_GENERAL_STREAM0_MODE = 5,
	PARAM_GENERAL_STREAM1_MODE = 6,
	//Audio,
	PARAM_GENERAL_STREAM2_MODE = 7,
	PARAM_AUDIO_STEREO_MODE = 8,
	PARAM_AUDIO_SAMPLE_RATE = 9,
	PARAM_AUDIO_LEFT_CHANNEL_VOLUME_LEVEL = 10,
	PARAM_AUDIO_RIGHT_CHANNEL_VOLUME_LEVEL = 11,
	//Image,
	PARAM_IMAGE_FORMAT = 12,
	PARAM_IMAGE_RESOLUTION = 13,
	PARAM_IMAGE_FPS = 14,
	PARAM_IMAGE_AGC = 15,
	PARAM_IMAGE_QUALITY = 16,
	PARAM_IMAGE_FLICKER_DETECTION = 17,
	//Depth,
	PARAM_DEPTH_FORMAT = 18,
	PARAM_DEPTH_RESOLUTION = 19,
	PARAM_DEPTH_FPS = 20,
	PARAM_DEPTH_AGC = 21,
	PARAM_DEPTH_HOLE_FILTER = 22,
	PARAM_DEPTH_MIRROR = 23,
	PARAM_DEPTH_DECIMATION = 24,
	//IR,
	PARAM_IR_FORMAT = 25,
	PARAM_IR_RESOLUTION = 26,
	PARAM_IR_FPS = 27,
	PARAM_IR_AGC = 28,
	PARAM_IR_QUALITY = 29,
	//Misc,
	PARAM_AUDIO_LEFT_CHANNEL_MUTE = 33,
	PARAM_AUDIO_RIGHT_CHANNEL_MUTE = 34,
	PARAM_AUDIO_MICROPHONE_IN = 35,
	PARAM_DEPTH_GMC_MODE = 36,
	PARAM_DEPTH_WHITE_BALANCE_ENABLE = 45,

	//Image Crop
	PARAM_IMAGE_CROP_SIZE_X = 46,
	PARAM_IMAGE_CROP_SIZE_Y = 47,
	PARAM_IMAGE_CROP_OFFSET_X = 48,
	PARAM_IMAGE_CROP_OFFSET_Y = 49,
	PARAM_IMAGE_CROP_ENABLE = 50,
	//Depth Crop
	PARAM_DEPTH_CROP_SIZE_X = 51,
	PARAM_DEPTH_CROP_SIZE_Y = 52,

	PARAM_DEPTH_CROP_OFFSET_X = 53,
	PARAM_DEPTH_CROP_OFFSET_Y = 54,
	PARAM_DEPTH_CROP_ENABLE = 55,
	//IR Crop
	PARAM_IR_CROP_SIZE_X = 56,
	PARAM_IR_CROP_SIZE_Y = 57,
	PARAM_IR_CROP_OFFSET_X = 58,
	PARAM_IR_CROP_OFFSET_Y = 59,
	PARAM_IR_CROP_ENABLE = 60,


	PARAM_APC_ENABLE = 62,

	PARAM_DEPTH_AGC_BIN0_LOW = 63,
	PARAM_DEPTH_AGC_BIN0_HIGH = 64,
	PARAM_DEPTH_AGC_BIN1_LOW = 65,
	PARAM_DEPTH_AGC_BIN1_HIGH = 66,
	PARAM_DEPTH_AGC_BIN2_LOW = 67,
	PARAM_DEPTH_AGC_BIN2_HIGH = 68,
	PARAM_DEPTH_AGC_BIN3_LOW = 69,
	PARAM_DEPTH_AGC_BIN3_HIGH = 70,

	PARAM_IMAGE_MIRROR = 71,
	PARAM_IR_MIRROR = 72,
	PARAM_IMAGE_SHARPNESS = 76,
	PARAM_IMAGE_AUTO_WHITE_BALANCE_MODE = 77,
	PARAM_IMAGE_COLOR_TEMPERATURE = 78,
	PARAM_IMAGE_BACK_LIGHT_COMPENSATION = 79,
	PARAM_IMAGE_AUTO_EXPOSURE_MODE = 80,
	PARAM_IMAGE_EXPOSURE_BAR = 81,
	PARAM_IMAGE_LOW_LIGHT_COMPENSATION_MODE = 82,
	PARAM_DEPTH_CLOSE_RANGE = 84,
} EConfig_Params;

typedef enum XnExecuter
{
	XN_EXECUTER_NONE = 0,
	XN_EXECUTER_FW = 1,
	XN_EXECUTER_HOST = 2,
} XnExecuter;

#endif
