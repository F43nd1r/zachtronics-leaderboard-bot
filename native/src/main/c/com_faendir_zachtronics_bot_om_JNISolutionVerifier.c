#include "com_faendir_zachtronics_bot_om_JNISolutionVerifier.h"
#include "verifier.h"
#include <limits.h>
#include <string.h>

JNIEXPORT jstring JNICALL Java_com_faendir_zachtronics_bot_om_JNISolutionVerifier_getPuzzleNameFromSolution
  (JNIEnv *env, jclass cls, jbyteArray jSolution) {
    (void)cls;

    jbyte *solution = (*env)->GetByteArrayElements(env, jSolution, NULL);
    const int solution_length = (*env)->GetArrayLength(env, jSolution);

    int puzzle_name_length;
    const char *puzzle_name_pointer = verifier_find_puzzle_name_in_solution_bytes((const char*) solution, solution_length, &puzzle_name_length);
    if (puzzle_name_pointer == 0 || puzzle_name_length > 50) {
        (*env)->ReleaseByteArrayElements(env, jSolution, solution, JNI_ABORT);
        return NULL;
    }
    char puzzle_name[puzzle_name_length + 1];
    memcpy(puzzle_name, puzzle_name_pointer, (size_t) puzzle_name_length);
    (*env)->ReleaseByteArrayElements(env, jSolution, solution, JNI_ABORT);
    puzzle_name[puzzle_name_length] = '\0';
    jstring result = (*env)->NewStringUTF(env, puzzle_name);
    return result;
}

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