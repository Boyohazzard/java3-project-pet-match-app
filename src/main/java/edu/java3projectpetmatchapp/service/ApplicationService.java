package edu.java3projectpetmatchapp.service;

import edu.java3projectpetmatchapp.dto.PetApplicationForm;
import edu.java3projectpetmatchapp.entity.Application;
import edu.java3projectpetmatchapp.enums.ApplicationStatus;
import edu.java3projectpetmatchapp.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository appRepo;
    private final ApplicationCacheService appCacheService;

    public Application getAppById(Long id) {
        return appRepo.findAppById(id)
                .orElseThrow(() -> new NoSuchElementException("No application found with ID: " + id));
    }

    public List<Application> getAllApplications() { return appRepo.findAll(); }

    public void registerNewApplication(PetApplicationForm form) {
        Application app = new Application();
        app.setUser(form.getUser());
        app.setPet(form.getPet());
        app.setYardAccess(form.getYardAccess());
        app.setOtherPets(form.getOtherPets());
        app.setHouseholdSituation(form.getHouseholdSituation());
        app.setHometype(form.getHomeType());
        app.setAdditionalInfo(form.getAdditionalInfo());
        app.setDateAppReceived(LocalDate.now());

        appRepo.save(app);
    }

    public List<Application> getAllApplicationsSorted(String sortField, Sort.Direction direction) {
        List<Application> apps = new ArrayList<>(appCacheService.getAllApplications());
        if (!isValidSortField(sortField)) sortField = "dateAppReceived";
        apps.sort(getComparator(sortField, direction));
        return apps;
    }

    private boolean isValidSortField(String field) {
        return List.of("email", "petName", "dateAppReceived", "status").contains(field);
    }

    private Comparator<Application> getComparator(String field, Sort.Direction direction) {
        Comparator<Application> comparator = switch (field) {
            case "petName" -> Comparator.comparing(
                    app -> app.getPet() != null ? app.getPet().getPetName() : null,
                    Comparator.nullsLast(String::compareToIgnoreCase)
            );
            case "email" -> Comparator.comparing(
                    app -> app.getUser() != null ? app.getUser().getEmail() : null,
                    Comparator.nullsLast(String::compareToIgnoreCase)
            );
            case "status" -> Comparator.comparing(Application::getApplicationStatus);
            default -> Comparator.comparing(Application::getDateAppReceived);
        };

        return direction == Sort.Direction.DESC ? comparator.reversed() : comparator;
    }

    public void updateApplicationStatus(Long applicationId, ApplicationStatus newStatus) {
        Application application = getAppById(applicationId);
        application.setApplicationStatus(newStatus);
        appRepo.save(application);
        appCacheService.evictAllApplications(); // Clear cache after update
    }

    public void deleteApplication(Long id) {
        Application app = appRepo.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Application not found."));

        appRepo.delete(app);
    }
}