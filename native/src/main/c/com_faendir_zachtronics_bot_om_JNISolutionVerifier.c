#include "com_faendir_zachtronics_bot_om_JNISolutionVerifier.h"
#include "verifier.h"
#include <stdbool.h>

JNIEXPORT jlong JNICALL Java_com_faendir_zachtronics_bot_om_JNISolutionVerifier_prepareVerifier
    (JNIEnv *env, jclass cls, jbyteArray jPuzzle, jbyteArray jSolution) {
    (void)cls;

    jbyte *puzzle = (*env)->GetByteArrayElements(env, jPuzzle, NULL);
    const int puzzle_length = (*env)->GetArrayLength(env, jPuzzle);
    jbyte *solution = (*env)->GetByteArrayElements(env, jSolution, NULL);
    const int solution_length = (*env)->GetArrayLength(env, jSolution);
    void *verifier = verifier_create_from_bytes((const char*) puzzle, puzzle_length, (const char*) solution, solution_length);
    (*env)->ReleaseByteArrayElements(env, jPuzzle, puzzle, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, jSolution, solution, JNI_ABORT);
    return (jlong) verifier;
}

JNIEXPORT void JNICALL Java_com_faendir_zachtronics_bot_om_JNISolutionVerifier_closeVerifier
    (JNIEnv *env, jclass cls, jlong jVerifier) {
    (void)env;
    (void)cls;

    void *verifier = (void*) jVerifier;
    verifier_destroy(verifier);
}

static bool throw_if_error(JNIEnv *env, void* verifier) {
    const char *error = verifier_error(verifier);
    if (error) {
        jclass exc = (*env)->FindClass(env, "com/faendir/zachtronics/bot/om/OmSimException");
        (*env)->ThrowNew(env, exc, error);
        return true;
    }
    return false;
}

JNIEXPORT jint JNICALL Java_com_faendir_zachtronics_bot_om_JNISolutionVerifier_getMetric
    (JNIEnv *env, jclass cls, jlong jVerifier, jstring jMetric) {
    (void)cls;

    void *verifier = (void*) jVerifier;
    const char *metric = (*env)->GetStringUTFChars(env, jMetric, 0);
    int result = verifier_evaluate_metric(verifier, metric);
    throw_if_error(env, verifier);
    return result;
}

JNIEXPORT jdouble JNICALL Java_com_faendir_zachtronics_bot_om_JNISolutionVerifier_getApproximateMetric
    (JNIEnv *env, jclass cls, jlong jVerifier, jstring jMetric) {
    (void)cls;

    void *verifier = (void*) jVerifier;
    const char *metric = (*env)->GetStringUTFChars(env, jMetric, 0);
    int result = verifier_evaluate_approximate_metric(verifier, metric);
    throw_if_error(env, verifier);
    return result;
}

JNIEXPORT void JNICALL Java_com_faendir_zachtronics_bot_om_JNISolutionVerifier_clearError
    (JNIEnv *env, jclass cls, jlong jVerifier) {
    (void)env;
    (void)cls;

    void *verifier = (void*) jVerifier;
    verifier_error_clear(verifier);
}