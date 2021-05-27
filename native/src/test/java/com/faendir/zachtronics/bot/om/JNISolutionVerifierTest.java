package com.faendir.zachtronics.bot.om;


import static org.junit.jupiter.api.Assertions.assertEquals;

class JNISolutionVerifierTest {

    @org.junit.jupiter.api.Test
    void getHeight() {
        JNISolutionVerifier verifier = new JNISolutionVerifier();
        int height = verifier.getHeight(
                getClass().getClassLoader().getResource("P009.puzzle").getFile(),
                getClass().getClassLoader().getResource("Face_Powder_Height_1.solution").getFile()
        );
        assertEquals(1, height);
    }
}