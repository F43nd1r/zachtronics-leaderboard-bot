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
     int result = verifier_evaluate_metric(verifier, "height");
     const char *error = verifier_error(verifier);
     verifier_destroy(verifier);
     if(error) {
         (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), error);
         result = INT_MAX;
     }
    return result;
}

JNIEXPORT jint JNICALL Java_com_faendir_zachtronics_bot_om_JNISolutionVerifier_getWidth
   (JNIEnv *env, jobject obj, jstring jPuzzle, jstring jSolution) {
     const char *puzzle = (*env)->GetStringUTFChars(env, jPuzzle, 0);
     const char *solution = (*env)->GetStringUTFChars(env, jSolution, 0);
     void *verifier = verifier_create(puzzle, solution);
     (*env)->ReleaseStringUTFChars(env, jPuzzle, puzzle);
     (*env)->ReleaseStringUTFChars(env, jSolution, solution);
     int result = verifier_evaluate_metric(verifier, "width*2");
     const char *error = verifier_error(verifier);
     verifier_destroy(verifier);
     if(error) {
         (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), error);
         result = INT_MAX;
     }
     return result;
 }

JNIEXPORT jint JNICALL Java_com_faendir_zachtronics_bot_om_JNISolutionVerifier_getCost
  (JNIEnv *env, jobject obj, jstring jPuzzle, jstring jSolution) {
    const char *puzzle = (*env)->GetStringUTFChars(env, jPuzzle, 0);
    const char *solution = (*env)->GetStringUTFChars(env, jSolution, 0);
    void *verifier = verifier_create(puzzle, solution);
    (*env)->ReleaseStringUTFChars(env, jPuzzle, puzzle);
    (*env)->ReleaseStringUTFChars(env, jSolution, solution);
     int result = verifier_evaluate_metric(verifier, "cost");
     const char *error = verifier_error(verifier);
     verifier_destroy(verifier);
     if(error) {
         (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), error);
         result = INT_MAX;
     }
    return result;
}

JNIEXPORT jint JNICALL Java_com_faendir_zachtronics_bot_om_JNISolutionVerifier_getCycles
  (JNIEnv *env, jobject obj, jstring jPuzzle, jstring jSolution) {
    const char *puzzle = (*env)->GetStringUTFChars(env, jPuzzle, 0);
    const char *solution = (*env)->GetStringUTFChars(env, jSolution, 0);
    void *verifier = verifier_create(puzzle, solution);
    (*env)->ReleaseStringUTFChars(env, jPuzzle, puzzle);
    (*env)->ReleaseStringUTFChars(env, jSolution, solution);
     int result = verifier_evaluate_metric(verifier, "cycles");
     const char *error = verifier_error(verifier);
     verifier_destroy(verifier);
     if(error) {
         (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), error);
         result = INT_MAX;
     }
    return result;
}

JNIEXPORT jint JNICALL Java_com_faendir_zachtronics_bot_om_JNISolutionVerifier_getArea
  (JNIEnv *env, jobject obj, jstring jPuzzle, jstring jSolution) {
    const char *puzzle = (*env)->GetStringUTFChars(env, jPuzzle, 0);
    const char *solution = (*env)->GetStringUTFChars(env, jSolution, 0);
    void *verifier = verifier_create(puzzle, solution);
    (*env)->ReleaseStringUTFChars(env, jPuzzle, puzzle);
    (*env)->ReleaseStringUTFChars(env, jSolution, solution);
     int result = verifier_evaluate_metric(verifier, "area (approximate)");
     const char *error = verifier_error(verifier);
     verifier_destroy(verifier);
     if(error) {
         (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), error);
         result = INT_MAX;
     }
    return result;
}

JNIEXPORT jint JNICALL Java_com_faendir_zachtronics_bot_om_JNISolutionVerifier_getInstructions
  (JNIEnv *env, jobject obj, jstring jPuzzle, jstring jSolution) {
    const char *puzzle = (*env)->GetStringUTFChars(env, jPuzzle, 0);
    const char *solution = (*env)->GetStringUTFChars(env, jSolution, 0);
    void *verifier = verifier_create(puzzle, solution);
    (*env)->ReleaseStringUTFChars(env, jPuzzle, puzzle);
    (*env)->ReleaseStringUTFChars(env, jSolution, solution);
     int result = verifier_evaluate_metric(verifier, "instructions");
     const char *error = verifier_error(verifier);
     verifier_destroy(verifier);
     if(error) {
         (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), error);
         result = INT_MAX;
     }
    return result;
}

JNIEXPORT jdouble JNICALL Java_com_faendir_zachtronics_bot_om_JNISolutionVerifier_getRate
  (JNIEnv *env, jobject obj, jstring jPuzzle, jstring jSolution) {
    const char *puzzle = (*env)->GetStringUTFChars(env, jPuzzle, 0);
    const char *solution = (*env)->GetStringUTFChars(env, jSolution, 0);
    void *verifier = verifier_create(puzzle, solution);
    (*env)->ReleaseStringUTFChars(env, jPuzzle, puzzle);
    (*env)->ReleaseStringUTFChars(env, jSolution, solution);
     double result = ((double) verifier_evaluate_metric(verifier, "throughput cycles")) / verifier_evaluate_metric(verifier, "throughput outputs");
     const char *error = verifier_error(verifier);
     verifier_destroy(verifier);
     if(error) {
         (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), error);
         result = INT_MAX;
     }
    return result;
}