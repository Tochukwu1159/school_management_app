package examination.teacherAndStudents.objectMapper;

import examination.teacherAndStudents.dto.TransportRequest;
import examination.teacherAndStudents.dto.TransportResponse;
import examination.teacherAndStudents.entity.Bus;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
    @RequiredArgsConstructor
    public class TransportMapper {
        private final ModelMapper modelMapper;

        public Bus mapToTransport(TransportRequest transportRequest) {
            return modelMapper.map(transportRequest, Bus.class);
        }

        public TransportResponse mapToTransportResponse(Bus transport) {
            return modelMapper.map(transport, TransportResponse.class);
        }


        public void updateTransportFromRequest(Bus transport, TransportRequest transportRequest) {

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

