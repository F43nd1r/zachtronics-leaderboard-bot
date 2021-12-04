#include "com_faendir_zachtronics_bot_om_JNISolutionVerifier.h"
#include "verifier.h"
#include <limits.h>

JNIEXPORT jlong JNICALL Java_com_faendir_zachtronics_bot_om_JNISolutionVerifier_prepareVerifier
    (JNIEnv *env, jclass cls, jstring jPuzzle, jstring jSolution) {
    (void)cls;

    const char *puzzle = (*env)->GetStringUTFChars(env, jPuzzle, 0);
    const char *solution = (*env)->GetStringUTFChars(env, jSolution, 0);
    void *verifier = verifier_create(puzzle, solution);
    (*env)->ReleaseStringUTFChars(env, jPuzzle, puzzle);
    (*env)->ReleaseStringUTFChars(env, jSolution, solution);
    return (jlong) verifier;
}

JNIEXPORT void JNICALL Java_com_faendir_zachtronics_bot_om_JNISolutionVerifier_closeVerifier
    (JNIEnv *env, jclass cls, jlong jVerifier) {
    (void)env;
    (void)cls;

    void *verifier = (void*) jVerifier;
    verifier_destroy(verifier);
}

JNIEXPORT jint JNICALL Java_com_faendir_zachtronics_bot_om_JNISolutionVerifier_getMetric
    (JNIEnv *env, jclass cls, jlong jVerifier, jstring jMetric) {
    (void)cls;

    void *verifier = (void*) jVerifier;
    const char *metric = (*env)->GetStringUTFChars(env, jMetric, 0);
    int result = verifier_evaluate_metric(verifier, metric);
    const char *error = verifier_error(verifier);
    if (error) {
        (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), error);
        result = INT_MAX;
    }
    return result;
}