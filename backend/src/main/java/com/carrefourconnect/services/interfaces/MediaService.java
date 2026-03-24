package com.carrefourconnect.services.interfaces;

import com.carrefourconnect.dtos.MediaDTO;
import java.util.List;
import java.util.UUID;

public interface MediaService {
    MediaDTO findById(UUID id);
    List<MediaDTO> findAll();
    MediaDTO save(MediaDTO dto);
    MediaDTO update(UUID id, MediaDTO dto);
    void delete(UUID id);
    List<MediaDTO> findByCommerce(UUID commerceId);
}
