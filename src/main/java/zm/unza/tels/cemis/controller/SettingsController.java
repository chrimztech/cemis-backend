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
        if (patch.getOrgName()   != null) settings.setOrgName(patch.getOrgName());
        if (patch.getOrgPrefix() != null) settings.setOrgPrefix(patch.getOrgPrefix());
        return ResponseEntity.ok(settingsRepository.save(settings));
    }
}
