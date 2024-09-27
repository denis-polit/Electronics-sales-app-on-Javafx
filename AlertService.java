public interface AlertService {
    void showSuccessAlert(String message);
    void showErrorAlert(String errorMessage);
    void showIpBlockedAlert();
    void showBlockedAccountAlert(String reason);
    void showInfoAlert(String title, String header, String content);
    boolean showConfirmationAlert(String message);
}