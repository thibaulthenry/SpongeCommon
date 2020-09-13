package org.spongepowered.common.adventure;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.adventure.ComponentTemplate;

public final class SpongeComponentTemplateTypeSerializer implements TypeSerializer<ComponentTemplate> {

    @Override
    public @Nullable ComponentTemplate deserialize(@NonNull final TypeToken<?> type, @NonNull final ConfigurationNode value)
            throws ObjectMappingException {
        final String templateString = value.getString();
        if (templateString != null) {
            return new SpongeComponentTemplate(templateString);
        }
        return null;
    }

    @Override
    public void serialize(@NonNull final TypeToken<?> type, @Nullable final ComponentTemplate obj, @NonNull final ConfigurationNode value)
            throws ObjectMappingException {
        if (obj == null) {
            value.setValue(null);
        } else {
            value.setValue(obj.templateString());
        }
    }

}
