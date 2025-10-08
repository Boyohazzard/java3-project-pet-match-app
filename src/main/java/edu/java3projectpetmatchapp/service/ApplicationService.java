package edu.java3projectpetmatchapp.service;

import edu.java3projectpetmatchapp.dto.PetApplicationForm;
import edu.java3projectpetmatchapp.entity.Application;
import edu.java3projectpetmatchapp.repository.ApplicationRepository;
import edu.java3projectpetmatchapp.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository appRepo;
    @Autowired
    private PetRepository petRepo;

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
}
