package zm.unza.tels.cemis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zm.unza.tels.cemis.entity.OrgSettings;
import zm.unza.tels.cemis.repository.OrgSettingsRepository;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final OrgSettingsRepository settingsRepository;

    @GetMapping
    public OrgSettings get() {
        return settingsRepository.findById(true).orElseGet(OrgSettings::new);
    }

    @PutMapping
    public ResponseEntity<OrgSettings> update(@RequestBody OrgSettings patch) {
        var settings = settingsRepository.findById(true).orElseGet(OrgSettings::new);
        if (patch.getOrgName()        != null) settings.setOrgName(patch.getOrgName());
        if (patch.getOrgPrefix()      != null) settings.setOrgPrefix(patch.getOrgPrefix());
        if (patch.getSignatory1Name() != null) settings.setSignatory1Name(patch.getSignatory1Name());
        if (patch.getSignatory1Title()!= null) settings.setSignatory1Title(patch.getSignatory1Title());
        if (patch.getSignatory2Name() != null) settings.setSignatory2Name(patch.getSignatory2Name());
        if (patch.getSignatory2Title()!= null) settings.setSignatory2Title(patch.getSignatory2Title());
        if (patch.getTemplateLayout() != null) settings.setTemplateLayout(patch.getTemplateLayout());
        return ResponseEntity.ok(settingsRepository.save(settings));
    }
}
