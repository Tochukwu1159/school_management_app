package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.BiometricVerificationResult;

public interface BiometricService {
    /**
     * Verifies a thumbprint against stored templates
     * @param thumbprintData Raw thumbprint data
     * @param staffId Staff identifier
     * @return Verification result with score
     */
    BiometricVerificationResult verifyThumbprint(byte[] thumbprintData, Long staffId);

    /**
     * Enrolls a new thumbprint template for staff
     */
    void enrollThumbprint(byte[] thumbprintData, Long staffId);
}

