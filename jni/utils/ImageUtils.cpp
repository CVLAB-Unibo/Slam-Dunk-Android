#include <jni.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include <arm_neon.h>

extern "C"
{
	JNIEXPORT void JNICALL Java_it_unibo_slam_utils_ImageUtils_convertNV21toBGRNativeC(JNIEnv *env, jobject obj,
			jbyteArray yuv, jbyteArray bgr, jint width, jint height);
	JNIEXPORT void JNICALL Java_it_unibo_slam_utils_ImageUtils_convertNV21toBGRNativeNEON(JNIEnv *env, jobject obj,
			jbyteArray yuv, jbyteArray bgr, jint width, jint height);
}

void convertNV21toBGRNativeC(unsigned char* yuv, unsigned char* bgr, int width, int height);
void storePixelC(unsigned char* &dst, int iR, int iG, int iB);

void convertNV21toBGRNativeNEON(unsigned char* yuv, unsigned char* bgr, int width, int height);
void storePixelNEON(unsigned char* dst, uint8x8x3_t& pblock, uint8x8_t const& r, uint8x8_t const& g, uint8x8_t const& b);

JNIEXPORT void JNICALL Java_it_unibo_slam_utils_ImageUtils_convertNV21toBGRNativeC(JNIEnv *env, jobject obj,
			jbyteArray yuv, jbyteArray bgr, jint width, jint height)
{
	jbyte *tempYUV = env->GetByteArrayElements(yuv, NULL);
	jbyte *tempBGR = env->GetByteArrayElements(bgr, NULL);

	convertNV21toBGRNativeC(reinterpret_cast<unsigned char*>(tempYUV), reinterpret_cast<unsigned char*>(tempBGR), width, height);

	env->ReleaseByteArrayElements(yuv, tempYUV, 0);
	env->ReleaseByteArrayElements(bgr, tempBGR, 0);
}

void convertNV21toBGRNativeC(unsigned char* const yuv, unsigned char* bgr, int width, int height)
{
	unsigned char* dst0 = bgr;
	unsigned char const* y0 = yuv;
	unsigned char const* uv = yuv + (width * height);
	const int halfHeight = height >> 1;
	const int halfWidth = width >> 1;
	const int widthMul = width * 3;

	int Y00, Y01, Y10, Y11;
	int V, U;
	int tR, tG, tB;

	for (int h = 0; h < halfHeight; ++h)
	{
		unsigned char const* y1 = y0 + width;
		unsigned char* dst1 = dst0 + widthMul;

		for (int w = 0; w < halfWidth; ++w)
		{
			Y00 = (*y0++) - 16;
			Y01 = (*y0++) - 16;
			Y10 = (*y1++) - 16;
			Y11 = (*y1++) - 16;

			V = (*uv++) - 128;
			U = (*uv++) - 128;

			Y00 = (Y00 > 0) ? (298 * Y00) : 0;
			Y01 = (Y01 > 0) ? (298 * Y01) : 0;
			Y10 = (Y10 > 0) ? (298 * Y10) : 0;
			Y11 = (Y11 > 0) ? (298 * Y11) : 0;

			tR = 128 + 409 * V;
			tG = 128 - 100 * U - 208 * V;
			tB = 128 + 516 * U;

			storePixelC(dst0, Y00 + tR, Y00 + tG, Y00 + tB);
			storePixelC(dst0, Y01 + tR, Y01 + tG, Y01 + tB);
			storePixelC(dst1, Y10 + tR, Y10 + tG, Y10 + tB);
			storePixelC(dst1, Y11 + tR, Y11 + tG, Y11 + tB);
		}

		y0 = y1;
		dst0 = dst1;
	}
}

void storePixelC(unsigned char* &dst, int iR, int iG, int iB)
{
	*dst++ = (iB > 0) ? (iB < 65535 ? (iB >> 8) : 0xff) : 0;
	*dst++ = (iG > 0) ? (iG < 65535 ? (iG >> 8) : 0xff) : 0;
	*dst++ = (iR > 0) ? (iR < 65535 ? (iR >> 8) : 0xff) : 0;
}

JNIEXPORT void JNICALL Java_it_unibo_slam_utils_ImageUtils_convertNV21toBGRNativeNEON(JNIEnv *env, jobject obj,
			jbyteArray yuv, jbyteArray bgr, jint width, jint height)
{
	jbyte *tempYUV = env->GetByteArrayElements(yuv, NULL);
	jbyte *tempBGR = env->GetByteArrayElements(bgr, NULL);

	convertNV21toBGRNativeNEON(reinterpret_cast<unsigned char*>(tempYUV), reinterpret_cast<unsigned char*>(tempBGR), width, height);

	env->ReleaseByteArrayElements(yuv, tempYUV, 0);
	env->ReleaseByteArrayElements(bgr, tempBGR, 0);
}

void convertNV21toBGRNativeNEON(unsigned char* const yuv, unsigned char* bgr, int width, int height)
{
	unsigned char const* y = yuv;
	unsigned char const* uv = yuv + (width*height);
	int const itHeight = height >> 1;
	int const itWidth = width >> 3;
	int const stride = width * 3;
	int const dst_pblock_stride = 8 * 3;
	uint8x8x3_t pblock = uint8x8x3_t();
	uint8x8_t const Yshift = vdup_n_u8(16);
	int16x8_t const half = (const int16x8_t)vdupq_n_u16(128);
	int32x4_t const rounding = vdupq_n_s32(128);

	uint16x8_t t;
	for (int j = 0; j < itHeight; ++j, y += width, bgr += stride)
	{
		for (int i = 0; i < itWidth; ++i, y += 8, uv += 8, bgr += dst_pblock_stride)
		{
			t = vmovl_u8(vqsub_u8(vld1_u8(y), Yshift));
			int32x4_t const Y00 = (const int32x4_t)vmulq_n_u32(vmovl_u16(vget_low_u16(t)), 298);
			int32x4_t const Y01 = (const int32x4_t)vmulq_n_u32(vmovl_u16(vget_high_u16(t)), 298);
			t = vmovl_u8(vqsub_u8(vld1_u8(y + width), Yshift));
			int32x4_t const Y10 = (const int32x4_t)vmulq_n_u32(vmovl_u16(vget_low_u16(t)), 298);
			int32x4_t const Y11 = (const int32x4_t)vmulq_n_u32(vmovl_u16(vget_high_u16(t)), 298);

			t = (uint16x8_t)vsubq_s16((int16x8_t)vmovl_u8(vld1_u8(uv)), half);
			int16x4x2_t const UV = vuzp_s16(vget_low_s16((int16x8_t)t), vget_high_s16((int16x8_t)t));

			int32x4_t const tR = vmlal_n_s16(rounding, UV.val[0], 409);
			int32x4_t const tG = vmlal_n_s16(vmlal_n_s16(rounding, UV.val[1], -100), UV.val[0], -208);
			int32x4_t const tB = vmlal_n_s16(rounding, UV.val[1], 516);
			int32x4x2_t const R = vzipq_s32(tR, tR);
			int32x4x2_t const G = vzipq_s32(tG, tG);
			int32x4x2_t const B = vzipq_s32(tB, tB);

			// Upper 8 pixels
			storePixelNEON(bgr, pblock,
					vshrn_n_u16(vcombine_u16(vqmovun_s32(vaddq_s32(R.val[0], Y00)), vqmovun_s32(vaddq_s32(R.val[1], Y01))), 8),
					vshrn_n_u16(vcombine_u16(vqmovun_s32(vaddq_s32(G.val[0], Y00)), vqmovun_s32(vaddq_s32(G.val[1], Y01))), 8),
					vshrn_n_u16(vcombine_u16(vqmovun_s32(vaddq_s32(B.val[0], Y00)), vqmovun_s32(vaddq_s32(B.val[1], Y01))), 8));

			// Lower 8 pixels
			storePixelNEON(bgr + stride, pblock,
					vshrn_n_u16(vcombine_u16(vqmovun_s32(vaddq_s32(R.val[0], Y10)), vqmovun_s32(vaddq_s32(R.val[1], Y11))), 8),
					vshrn_n_u16(vcombine_u16(vqmovun_s32(vaddq_s32(G.val[0], Y10)), vqmovun_s32(vaddq_s32(G.val[1], Y11))), 8),
					vshrn_n_u16(vcombine_u16(vqmovun_s32(vaddq_s32(B.val[0], Y10)), vqmovun_s32(vaddq_s32(B.val[1], Y11))), 8));
		}
	}
}

void storePixelNEON(unsigned char* dst, uint8x8x3_t& pblock, uint8x8_t const& r, uint8x8_t const& g, uint8x8_t const& b)
{
	pblock.val[0] = b;
	pblock.val[1] = g;
	pblock.val[2] = r;
	vst3_u8(dst, pblock);
}
