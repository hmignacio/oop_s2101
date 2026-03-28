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

public interface LoginDao {
    UserAccount authenticate(String username, String password);
    boolean addAccount(UserAccount account);
    
    // CHANGE THIS FROM void TO boolean
    boolean updatePassword(String username, String newPassword); 
    
    boolean deleteAccount(String username);
    List<UserAccount> getAllAccounts();
    
    boolean updateRole(String employeeId, String newRole);
}
