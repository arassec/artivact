package com.arassec.artivact.backend.service.creator.adapter.image.background;


import com.arassec.artivact.backend.service.creator.adapter.BaseAdapter;

import java.nio.file.Path;
import java.util.List;

/**
 * Base class for background-removal adapter implementations.
 */
public abstract class BaseBackgroundRemovalAdapter
        extends BaseAdapter<BackgroundRemovalInitParams, List<Path>> implements BackgroundRemovalAdapter {
}
