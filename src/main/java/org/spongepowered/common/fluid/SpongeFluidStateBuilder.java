package org.spongepowered.common.fluid;

import static net.minecraft.command.arguments.BlockStateParser.STATE_INVALID_PROPERTY_VALUE;
import static org.spongepowered.common.data.util.DataUtil.checkDataExists;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.fluid.FluidTypes;
import org.spongepowered.common.util.Constants;

import java.util.Objects;
import java.util.Optional;

public class SpongeFluidStateBuilder implements FluidState.Builder {

    private FluidState state = FluidTypes.EMPTY.get().getDefaultState();

    @Override
    public FluidState.Builder fluid(final FluidType fluidType) {
        this.state = Objects.requireNonNull(fluidType).getDefaultState();
        return this;
    }

    @Override
    public FluidState.Builder fromString(final String id) {
        this.state = this.parseString(id);
        return this;
    }

    @Override
    public <V> FluidState.Builder add(final Key<? extends Value<V>> key, final V value) {
        Objects.requireNonNull(this.state, "The fluid type must be set first");
        Objects.requireNonNull(key, "The key must not be null");
        Objects.requireNonNull(key, "The value must not be null");
        this.state = this.state.with(key, value).orElse(this.state);
        return this;
    }

    @Override
    public FluidState.Builder from(final FluidState holder) {
        this.state = holder;
        return this;
    }

    @Override
    public FluidState build() {
        Objects.requireNonNull(this.state, "There must be a FluidType specified.");
        return this.state;
    }

    @Override
    public FluidState.Builder reset() {
        this.state = null;
        return this;
    }

    @Override
    public Optional<FluidState> build(final DataView container) throws InvalidDataException {
        if (!container.contains(Constants.Fluids.FLUID_STATE)) {
            return Optional.empty();
        }
        checkDataExists(container, Constants.Fluids.FLUID_STATE);
        try {
            return container.getString(Constants.Fluids.FLUID_STATE).map(this::parseString);
        } catch (final Exception e) {
            throw new InvalidDataException("Could not retrieve a blockstate!", e);
        }
    }

    // The following is adapted from BlockStateParser

    private FluidState parseString(final String string) {
        final StringReader reader = new StringReader(string);
        try {
            final Fluid fluid = this.readFluid(reader);
            final IFluidState state;
            if (reader.canRead() && reader.peek() == '[') {
                state = this.readProperties(reader, fluid);
            } else {
                state = fluid.getDefaultState();
            }
            return (FluidState) state;
        } catch (final CommandSyntaxException e) {
            throw new IllegalArgumentException("The string " + string + " doe not parse into a valid FluidState", e);
        }
    }

    private Fluid readFluid(final StringReader reader) throws CommandSyntaxException {
        final int cursor = reader.getCursor();
        final ResourceLocation fluidKey = ResourceLocation.read(reader);
        return Registry.FLUID.getValue(fluidKey).orElseThrow(() -> {
            reader.setCursor(cursor);
            return BlockStateParser.STATE_BAD_ID.createWithContext(reader, fluidKey.toString());
        });
    }

    private net.minecraft.fluid.FluidState readProperties(final StringReader reader, final Fluid fluid) throws CommandSyntaxException {
        reader.skip();
        reader.skipWhitespace();

        IFluidState state = fluid.getDefaultState();
        final StateContainer<?, ?> stateContainer = fluid.getStateContainer();
        while(reader.canRead() && reader.peek() != ']') {
            reader.skipWhitespace();
            final int cursor = reader.getCursor();
            final String propertyKey = reader.readString();
            final IProperty<?> property = stateContainer.getProperty(propertyKey);
            if (property == null) {
                reader.setCursor(cursor);
                throw BlockStateParser.STATE_UNKNOWN_PROPERTY.createWithContext(reader, fluid.toString(), propertyKey);
            }

            reader.skipWhitespace();
            if (reader.canRead() && reader.peek() == '=') {
                reader.skip();
                reader.skipWhitespace();
                final int cursor1 = reader.getCursor();
                state = this.parseValue(state, reader, property, cursor1);
                reader.skipWhitespace();
                if (!reader.canRead()) {
                    continue;
                }

                if (reader.peek() == ',') {
                    reader.skip();
                    continue;
                }

                if (reader.peek() != ']') {
                    throw BlockStateParser.STATE_UNCLOSED.createWithContext(reader);
                }
                break;
            }

            throw BlockStateParser.STATE_NO_VALUE.createWithContext(reader, fluid.toString(), propertyKey);
        }

        if (reader.canRead()) {
            reader.skip();
        } else {
            throw BlockStateParser.STATE_UNCLOSED.createWithContext(reader);
        }

        return (net.minecraft.fluid.FluidState) state;
    }

    private <T extends Comparable<T>> IFluidState parseValue(
            final IFluidState state, final StringReader reader, final IProperty<T> property, final int cursor) throws CommandSyntaxException {
        final Optional<T> propertyValue = property.parseValue(reader.readString());
        if (propertyValue.isPresent()) {
            return state.with(property, propertyValue.get());
        } else {
            reader.setCursor(cursor);
            throw STATE_INVALID_PROPERTY_VALUE.createWithContext(reader, state.getFluid().toString(), property.getName(), cursor);
        }
    }

}
