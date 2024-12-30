package examination.teacherAndStudents.utils;

import examination.teacherAndStudents.dto.LoginRequest;
import examination.teacherAndStudents.dto.LoginResponse;
import examination.teacherAndStudents.dto.UserDto;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.AuthenticationFailedException;
import examination.teacherAndStudents.error_handler.SubscriptionExpiredException;
import examination.teacherAndStudents.error_handler.UserPasswordMismatchException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class loginReact {
}
//npm install crypto-js

/*
import React, { useState } from 'react';
        import CryptoJS from 'crypto-js';

function App() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [encryptedData, setEncryptedData] = useState('');

  const SECRET_KEY = "1234567890abcdef"; // 16-byte key for AES encryption

  const handleSubmit = (e) => {
        e.preventDefault();

        // Encrypt username and password
    const encryptedUsername = CryptoJS.AES.encrypt(username, SECRET_KEY).toString();
    const encryptedPassword = CryptoJS.AES.encrypt(password, SECRET_KEY).toString();

        // Create an object to send
    const dataToSend = {
                encryptedUsername,
                encryptedPassword
        };

        // Mock sending the data (you can replace this with an API call)
        setEncryptedData(dataToSend);
        console.log('Encrypted Data:', dataToSend);

        // Optionally, you can send this encrypted data to your backend API
        // fetch('/api/login', { method: 'POST', body: JSON.stringify(dataToSend), headers: { 'Content-Type': 'application/json' } })
        //   .then(response => response.json())
        //   .then(data => console.log(data))
        //   .catch(error => console.error('Error:', error));
    };

    return (
            <div className="App">
      <h2>Login</h2>
      <form onSubmit={handleSubmit}>
        <div>
          <label>Username:</label>
          <input
            type="text"
    value={username}
    onChange={(e) => setUsername(e.target.value)}
    required
            />
        </div>
        <div>
          <label>Password:</label>
          <input
            type="password"
    value={password}
    onChange={(e) => setPassword(e.target.value)}
    required
            />
        </div>
        <button type="submit">Login</button>
      </form>

      <div>
        <h3>Encrypted Data:</h3>
        <pre>{JSON.stringify(encryptedData, null, 2)}</pre>
      </div>
    </div>
  );
}

export default App;
*/

//public LoginResponse loginAdmin(LoginRequest loginRequest) {
//    System.out.println(loginRequest);
//    try {
//        // Decrypt the login request data (email and password)
//        String decryptedEmail = EncryptionUtil.decrypt(loginRequest.getEmail());
//        String decryptedPassword = EncryptionUtil.decrypt(loginRequest.getPassword());
//        System.out.println(decryptedEmail);
//        System.out.println(decryptedPassword);
//        // Authenticate the admin using the decrypted email and password
//        Authentication authenticate = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(decryptedEmail, decryptedPassword)
//        );
//
//        if (!authenticate.isAuthenticated()) {
//            throw new UserPasswordMismatchException("Wrong email or password");
//        }
//
//        // Fetch user details
//        Optional<User> userDetails = userRepository.findByEmail(decryptedEmail);
//
//        // Check if the subscription has expired
//        School school = userDetails.get().getSchool();
//        if (school.getSubscriptionExpiryDate() == null) {
//            throw new SubscriptionExpiredException("Not subscribed yet. Please subscribe to enjoy the services.");
//        }
//        if (school != null && !school.isSubscriptionValid()) {
//            throw new SubscriptionExpiredException("Your subscription has expired. Please renew your subscription.");
//        }
//
//        // Set the authentication context
//        SecurityContextHolder.getContext().setAuthentication(authenticate);
//
//        // Generate the token
//        String token = "Bearer " + jwtUtil.generateToken(decryptedEmail, userDetails.get().getSchool().getSubscriptionKey());
//
//        // Encrypt the token and user details before returning
//        String encryptedToken = EncryptionUtil.encrypt(token);
//        UserDto userDto = new UserDto();
//        userDto.setFirstName(EncryptionUtil.encrypt(userDetails.get().getFirstName()));
//        userDto.setLastName(EncryptionUtil.encrypt(userDetails.get().getLastName()));
//        userDto.setEmail(EncryptionUtil.encrypt(userDetails.get().getEmail()));
//
//        return new LoginResponse(encryptedToken, userDto);
//    } catch (BadCredentialsException e) {
//        // Handle the "Bad credentials" error here
//        throw new AuthenticationFailedException("Wrong email or password");
//    } catch (Exception e) {
//        // Handle other exceptions
//        throw new RuntimeException("Error during admin login: " + e.getMessage());
//    }
//}


//public LoginResponse loginUser(LoginRequest loginRequest) {
//    try {
//        // Decrypt the login request data (email and password)
//        String decryptedEmail = EncryptionUtil.decrypt(loginRequest.getEmail());
//        String decryptedPassword = EncryptionUtil.decrypt(loginRequest.getPassword());
//
//        // Authenticate the user using the decrypted email and password
//        Authentication authenticate = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(decryptedEmail, decryptedPassword)
//        );
//
//        if (!authenticate.isAuthenticated()) {
//            throw new UserPasswordMismatchException("Wrong email or password");
//        }
//
//        // Fetch user details
//        Optional<User> userDetails = userRepository.findByEmail(decryptedEmail);
//
//        // Check if the subscription has expired
//        School school = userDetails.get().getSchool();
//        if (school != null && !school.isSubscriptionValid()) {
//            throw new SubscriptionExpiredException("Your subscription has expired. Please renew your subscription.");
//        }
//
//        // Set the authentication context
//        SecurityContextHolder.getContext().setAuthentication(authenticate);
//
//        // Generate the token
//        String token = "Bearer " + jwtUtil.generateToken(decryptedEmail, userDetails.get().getSchool().getSubscriptionKey());
//
//        // Encrypt the token and user details before returning
//        String encryptedToken = EncryptionUtil.encrypt(token);
//        UserDto userDto = new UserDto();
//        userDto.setFirstName(EncryptionUtil.encrypt(userDetails.get().getFirstName()));
//        userDto.setLastName(EncryptionUtil.encrypt(userDetails.get().getLastName()));
//        userDto.setEmail(EncryptionUtil.encrypt(userDetails.get().getEmail()));
//
//        return new LoginResponse(encryptedToken, userDto);
//    } catch (BadCredentialsException e) {
//        // Handle the "Bad credentials" error here
//        throw new AuthenticationFailedException("Wrong email or password");
//    } catch (Exception e) {
//        // Handle other exceptions
//        throw new RuntimeException("Error during login: " + e.getMessage());
//    }
//}