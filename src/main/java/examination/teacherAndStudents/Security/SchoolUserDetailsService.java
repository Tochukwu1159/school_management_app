//package examination.teacherAndStudents.Security;
//
//import examination.teacherAndStudents.repository.SchoolRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//
//@Service
//public class SchoolUserDetailsService implements UserDetailsService {
//
//    @Autowired
//    private SchoolRepository schoolRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        return null;
//    }
//
////    @Override
////    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
////        School school = schoolRepository.findByEmail(email)
////                .orElseThrow(() -> new UsernameNotFoundException("School not found with email: " + email));
//////        return new User(school.getEmail(), school.getPassword(), new ArrayList<>());
////    }}
//}