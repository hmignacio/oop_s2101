/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package interfaces;

/**
 *
 * @author Vladimir Bernardo
 */
import model.UserAccount;
import java.util.List;

public interface UserAccountDao {
    UserAccount authenticate(String employeeNo, String password);
    boolean addAccount(UserAccount account);
    boolean updatePassword(String employeeNo, String newPassword);
    boolean deleteAccount(String employeeNo);
    List<UserAccount> getAllAccounts();
}
