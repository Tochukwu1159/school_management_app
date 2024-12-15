package examination.teacherAndStudents.objectMapper;

import examination.teacherAndStudents.dto.TransportRequest;
import examination.teacherAndStudents.dto.TransportResponse;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.Transport;
import examination.teacherAndStudents.entity.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
    @RequiredArgsConstructor
    public class TransportMapper {
        private final ModelMapper modelMapper;

        public Transport mapToTransport(TransportRequest transportRequest) {
            return modelMapper.map(transportRequest, Transport.class);
        }

        public TransportResponse mapToTransportResponse(Transport transport) {
            return modelMapper.map(transport, TransportResponse.class);
        }


        public void updateTransportFromRequest(Transport transport, TransportRequest transportRequest) {

            if (transportRequest.getVehicleNumber() != null) {
                transport.setVehicleNumber(transportRequest.getVehicleNumber());
            }
        }

//        public void addStudentsToTransport(Transport transport, List<User> students) {
//            Set<Profile> existingStudents = transport.getUserProfiles();
//            existingStudents.addAll(students);
//            transport.setUserProfiles(existingStudents);
//        }
    }

