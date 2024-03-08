package bul.nik.ldtesttask.exception;

public class PhoneNumberCheckException extends RuntimeException {
    public PhoneNumberCheckException(String phoneNumber, String problemDescription) {
        super(String.format("Problem with phone number %s: %n%s", phoneNumber, problemDescription));
    }
}
