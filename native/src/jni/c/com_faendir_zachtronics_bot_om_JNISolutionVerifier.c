#include "com_faendir_zachtronics_bot_om_JNISolutionVerifier.h"
#include "verifier.h"
#include <limits.h>

JNIEXPORT jint JNICALL Java_com_faendir_zachtronics_bot_om_JNISolutionVerifier_getHeight
  (JNIEnv *env, jobject obj, jstring jPuzzle, jstring jSolution) {
    const char *puzzle = (*env)->GetStringUTFChars(env, jPuzzle, 0);
    const char *solution = (*env)->GetStringUTFChars(env, jSolution, 0);
    void *verifier = verifier_create(puzzle, solution);
    (*env)->ReleaseStringUTFChars(env, jPuzzle, puzzle);
    (*env)->ReleaseStringUTFChars(env, jSolution, solution);
    const char *error = verifier_error(verifier);
    if(!error) {
        return verifier_evaluate_metric(verifier, "height");
    }
    (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), error);
    return INT_MAX;
}