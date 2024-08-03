package com.arassec.artivact.domain.creator.adapter.image.background;


import com.arassec.artivact.domain.creator.adapter.BaseAdapter;

import java.nio.file.Path;
import java.util.List;

/**
 * Base class for background-removal adapter implementations.
 */
public abstract class BaseBackgroundRemovalAdapter
        extends BaseAdapter<BackgroundRemovalInitParams, List<Path>> implements BackgroundRemovalAdapter {
}
