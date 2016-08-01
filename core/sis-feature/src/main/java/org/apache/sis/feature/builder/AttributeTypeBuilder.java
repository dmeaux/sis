/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.feature.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.lang.reflect.Array;
import org.opengis.util.GenericName;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.apache.sis.feature.DefaultAttributeType;
import org.apache.sis.feature.FeatureOperations;
import org.apache.sis.internal.util.CollectionsExt;
import org.apache.sis.internal.feature.AttributeConvention;
import org.apache.sis.internal.feature.Geometries;
import org.apache.sis.util.resources.Errors;
import org.apache.sis.util.Classes;
import org.apache.sis.util.ObjectConverters;
import org.apache.sis.util.UnconvertibleObjectException;

// Branch-dependent imports
import java.util.Objects;
import org.opengis.feature.AttributeType;
import org.opengis.feature.PropertyType;


/**
 * Describes one attribute of the {@code FeatureType} to be built by the enclosing {@code FeatureTypeBuilder}.
 * A different instance of {@code AttributeTypeBuilder} exists for each feature attribute to describe.
 * Those instances are created by {@link FeatureTypeBuilder#addAttribute(Class)}.
 *
 * @param <V> the class of attribute values.
 *
 * @author  Johann Sorel (Geomatys)
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.8
 * @version 0.8
 * @module
 *
 * @see org.apache.sis.feature.DefaultAttributeType
 * @see FeatureTypeBuilder#addAttribute(Class)
 */
public final class AttributeTypeBuilder<V> extends PropertyTypeBuilder {
    /**
     * The class of attribute values. Can not be changed after construction
     * because this value determines the parameterized type {@code <V>}.
     */
    private final Class<V> valueClass;

    /**
     * The default value for the attribute, or {@code null} if none.
     */
    private V defaultValue;

    /**
     * Whether this attribute will be used in a {@linkplain FeatureOperations#compound compound key} named
     * {@code "@identifier"}. If only one attribute has this flag and {@link FeatureTypeBuilder#idPrefix} and
     * {@code isSuffix} are null, then {@code "@identifier"} will be a {@linkplain FeatureOperations#link link}
     * to {@code idAttributes[0]}.
     *
     * @see #addRole(AttributeRole)
     */
    private boolean isIdentifier;

    /**
     * Builders for the characteristics associated to the attribute.
     */
    private final List<CharacteristicTypeBuilder<?>> characteristics;

    /**
     * Creates a new builder initialized to the values of the given builder.
     * This constructor is for {@link #setValueClass(Class)} implementation only.
     *
     * @throws UnconvertibleObjectException if the default value can not be converted to the given class.
     */
    private AttributeTypeBuilder(final AttributeTypeBuilder<?> builder, final Class<V> valueClass)
            throws UnconvertibleObjectException
    {
        super(builder);
        this.valueClass = valueClass;
        defaultValue = ObjectConverters.convert(builder.defaultValue, valueClass);
        isIdentifier = builder.isIdentifier;
        characteristics = builder.characteristics;
    }

    /**
     * Creates a new {@code AttributeType} builder for values of the given class.
     *
     * @param owner      the builder of the {@code FeatureType} for which to add the attribute.
     * @param valueClass the class of attribute values.
     */
    AttributeTypeBuilder(final FeatureTypeBuilder owner, final Class<V> valueClass) {
        super(owner, null);
        this.valueClass = valueClass;
        characteristics = new ArrayList<>();
    }

    /**
     * Creates a new {@code AttributeType} builder initialized to the values of an existing attribute.
     *
     * @param owner  the builder of the {@code FeatureType} for which to add the attribute.
     */
    AttributeTypeBuilder(final FeatureTypeBuilder owner, final AttributeType<V> template) {
        super(owner, template);
        minimumOccurs = template.getMinimumOccurs();
        maximumOccurs = template.getMaximumOccurs();
        valueClass    = template.getValueClass();
        defaultValue  = template.getDefaultValue();
        final Map<String, AttributeType<?>> tc = template.characteristics();
        characteristics = new ArrayList<>(tc.size());
        for (final AttributeType<?> c : tc.values()) {
            characteristics.add(new CharacteristicTypeBuilder<>(this, c));
        }
    }

    /**
     * Returns a default name to use if the user did not specified a name. The first letter will be changed to
     * lower case (unless the name looks like an acronym) for compliance with Java convention on attribute names.
     */
    @Override
    final String getDefaultName() {
        return Classes.getShortName(valueClass);
    }

    /**
     * Sets the {@code AttributeType} name as a generic name.
     * If another name was defined before this method call, that previous value will be discarded.
     *
     * @return {@code this} for allowing method calls chaining.
     */
    @Override
    public AttributeTypeBuilder<V> setName(final GenericName name) {
        super.setName(name);
        return this;
    }

    /**
     * Sets the {@code AttributeType} name as a simple string with the default scope.
     * The default scope is the value specified by the last call to
     * {@link FeatureTypeBuilder#setDefaultScope(String)}.
     * The name will be a {@linkplain org.apache.sis.util.iso.DefaultLocalName local name} if no default scope
     * has been specified, or a {@linkplain org.apache.sis.util.iso.DefaultScopedName scoped name} otherwise.
     *
     * @return {@code this} for allowing method calls chaining.
     */
    @Override
    public AttributeTypeBuilder<V> setName(final String localPart) {
        super.setName(localPart);
        return this;
    }

    /**
     * Sets the {@code AttributeType} name as a string in the given scope.
     * The name will be a {@linkplain org.apache.sis.util.iso.DefaultLocalName local name} if the given scope is
     * {@code null} or empty, or a {@linkplain org.apache.sis.util.iso.DefaultScopedName scoped name} otherwise.
     * If a {@linkplain FeatureTypeBuilder#setDefaultScope(String) default scope} has been specified, then the
     * {@code scope} argument overrides it.
     *
     * @return {@code this} for allowing method calls chaining.
     */
    @Override
    public AttributeTypeBuilder<V> setName(final String scope, final String localPart) {
        super.setName(scope, localPart);
        return this;
    }

    /**
     * Returns the class of attribute values.
     *
     * @return the class of attribute values.
     *
     * @see #setValueClass(Class)
     */
    public Class<V> getValueClass() {
        return valueClass;
    }

    /**
     * Sets the class of attribute values. Callers <strong>must</strong> use the builder returned by this method
     * instead of {@code this} builder after this method call, since the returned builder may be a new instance.
     *
     * @param  <N>   the compile-time value of the {@code type} argument.
     * @param  type  the new class of attribute values.
     * @return the attribute builder — <em>not necessarily this instance.</em>
     * @throws UnconvertibleObjectException if the {@linkplain #getDefaultValue() default value}
     *         can not be converted to the given {@code <N>} class.
     *
     * @see #getValueClass()
     */
    @SuppressWarnings("unchecked")
    public <N> AttributeTypeBuilder<N> setValueClass(final Class<N> type) throws UnconvertibleObjectException {
        final FeatureTypeBuilder owner = owner();
        ensureNonNull("type", type);
        if (type == valueClass) {
            return (AttributeTypeBuilder<N>) this;
        }
        final AttributeTypeBuilder<N> n = new AttributeTypeBuilder<>(this, type);
        for (final CharacteristicTypeBuilder<?> c : n.characteristics) {
            c.owner = n;
        }
        owner.replace(this, n);
        dispose();
        return n;
    }

    /**
     * Returns the default value for the attribute, or {@code null} if none.
     *
     * @return the default attribute value, or {@code null} if none.
     *
     * @see #setDefaultValue(Object)
     */
    public V getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default value for the attribute.
     *
     * @param  value  default attribute value, or {@code null} if none.
     * @return {@code this} for allowing method calls chaining.
     *
     * @see #getDefaultValue()
     */
    public AttributeTypeBuilder<V> setDefaultValue(final V value) {
        if (!Objects.equals(defaultValue, value)) {
            defaultValue = value;
            clearCache();
        }
        return this;
    }

    /**
     * Returns an enumeration of valid values for the attribute, or an empty array if none.
     *
     * @return valid values for the attribute, or an empty array if none.
     */
    @SuppressWarnings("unchecked")
    public V[] getValidValues() {
        final Collection<?> c = CollectionsExt.nonNull((Collection<?>)
                getCharacteristic(AttributeConvention.VALID_VALUES_CHARACTERISTIC));
        final V[] values = (V[]) Array.newInstance(valueClass, c.size());
        int index = 0;
        for (final Object value : c) {
            values[index++] = (V) value;        // ArrayStoreException if 'value' is not the expected type.
        }
        return values;
    }

    /**
     * Sets an enumeration of valid values for the attribute.
     *
     * <p>This is a convenience method for {@link #addCharacteristic(Class)} with a value
     * of type {@link Set} and a conventional name.</p>
     *
     * @param  values valid values.
     * @return {@code this} for allowing method calls chaining.
     *
     * @see #characteristics()
     * @see AttributeConvention#VALID_VALUES_CHARACTERISTIC
     */
    @SafeVarargs
    public final AttributeTypeBuilder<V> setValidValues(final V... values) {
        return setCharacteristic(AttributeConvention.VALID_VALUES_CHARACTERISTIC,
                Set.class, CollectionsExt.immutableSet(false, values));
    }

    /**
     * Sets the maximal length that characterizes the {@link CharSequence} values of this attribute.
     * While this characteristic can be applied to any kind of attribute, it is meaningful only with
     * character sequences.
     *
     * <p>This is a convenience method for {@link #addCharacteristic(Class)} with a value
     * of type {@link Integer} and a conventional name.</p>
     *
     * @param  length  maximal length of {@link CharSequence} attribute values, or {@code null}.
     * @return {@code this} for allowing method calls chaining.
     *
     * @see #characteristics()
     * @see AttributeConvention#MAXIMAL_LENGTH_CHARACTERISTIC
     */
    public AttributeTypeBuilder<V> setMaximalLength(final Integer length) {
        return setCharacteristic(AttributeConvention.MAXIMAL_LENGTH_CHARACTERISTIC, Integer.class, length);
    }

    /**
     * Sets the coordinate reference system that characterizes the values of this attribute.
     * While this characteristic can be applied to any kind of attribute, it is meaningful
     * only with georeferenced values like geometries or coverages.
     *
     * <p>This is a convenience method for {@link #addCharacteristic(Class)} with a value
     * of type {@link CoordinateReferenceSystem} and a conventional name.</p>
     *
     * @param  crs  coordinate reference system associated to attribute values, or {@code null}.
     * @return {@code this} for allowing method calls chaining.
     *
     * @see #characteristics()
     * @see AttributeConvention#CRS_CHARACTERISTIC
     */
    public AttributeTypeBuilder<V> setCRS(final CoordinateReferenceSystem crs) {
        return setCharacteristic(AttributeConvention.CRS_CHARACTERISTIC, CoordinateReferenceSystem.class, crs);
    }

    /**
     * Implementation of all getter methods for characteristics.
     */
    private Object getCharacteristic(final GenericName name) {
        for (final CharacteristicTypeBuilder<?> characteristic : characteristics) {
            if (name.equals(characteristic.getName())) {
                return characteristic.getDefaultValue();
            }
        }
        return null;
    }

    /**
     * Implementation of all setter methods for characteristics.
     */
    private <C> AttributeTypeBuilder<V> setCharacteristic(final GenericName name, final Class<C> type, final C value) {
        for (final CharacteristicTypeBuilder<?> characteristic : characteristics) {
            if (name.equals(characteristic.getName())) {
                characteristic.set(value);
                return this;
            }
        }
        addCharacteristic(type).setDefaultValue(value).setName(name);
        return this;
    }

    /**
     * Adds another attribute type that describes this attribute type.
     * See <cite>"Attribute characterization"</cite> in {@link DefaultAttributeType} Javadoc for more information.
     *
     * <p>Usage example:</p>
     * {@preformat java
     *     attribute.addCharacteristic(Unit.class).setName("Unit of measurement").setDefaultValue(SI.CELSIUS);
     * }
     *
     * The default characteristic name is the name of the given type, but callers should invoke one
     * of the {@code CharacteristicTypeBuilder.setName(…)} methods on the returned instance with a better name.
     *
     * @param  <C>   the compile-time type of {@code type} argument.
     * @param  type  the class of characteristic values.
     * @return a builder for a characteristic of this attribute.
     *
     * @see #characteristics()
     */
    public <C> CharacteristicTypeBuilder<C> addCharacteristic(final Class<C> type) {
        ensureNonNull("type", type);
        final CharacteristicTypeBuilder<C> characteristic = new CharacteristicTypeBuilder<>(this, type);
        characteristics.add(characteristic);
        clearCache();
        return characteristic;
    }

    /**
     * Adds another attribute type that describes this attribute type, using an existing one as a template.
     * See <cite>"Attribute characterization"</cite> in {@link DefaultAttributeType} Javadoc for more information.
     *
     * @param  <C>       the compile-time type of values in the {@code template} argument.
     * @param  template  an existing attribute type to use as a template.
     * @return a builder for a characteristic of this attribute, initialized with the values of the given template.
     *
     * @see #characteristics()
     */
    public <C> CharacteristicTypeBuilder<C> addCharacteristic(final AttributeType<C> template) {
        ensureNonNull("template", template);
        final CharacteristicTypeBuilder<C> characteristic = new CharacteristicTypeBuilder<>(this, template);
        characteristics.add(characteristic);
        clearCache();
        return characteristic;
    }

    /**
     * Flags this attribute as an input of one of the pre-defined operations managed by {@code FeatureTypeBuilder}.
     *
     * @param role the role to add to this attribute (shall not be null).
     */
    public void addRole(final AttributeRole role) {
        final FeatureTypeBuilder owner = owner();
        ensureNonNull("role", role);
        switch (role) {
            case IDENTIFIER_COMPONENT: {
                if (!isIdentifier) {
                    isIdentifier = true;
                    owner.identifierCount++;
                    owner.clearCache();         // The change does not impact this attribute itself.
                }
                break;
            }
            case DEFAULT_GEOMETRY: {
                if (owner.defaultGeometry != this) {
                    if (!Geometries.isKnownType(valueClass)) {
                        throw new IllegalStateException(errors().getString(Errors.Keys.UnsupportedImplementation_1, valueClass));
                    }
                    if (owner.defaultGeometry != null) {
                        throw new IllegalStateException(errors().getString(Errors.Keys.PropertyAlreadyExists_2,
                                owner.getDisplayName(), AttributeConvention.GEOMETRY_PROPERTY));
                    }
                    owner.defaultGeometry = this;
                    owner.clearCache();         // The change does not impact this attribute itself.
                }
                break;
            }
        }
    }

    /**
     * Returns {@code true} if {@link AttributeRole#IDENTIFIER_COMPONENT} has been associated to this attribute.
     */
    @Override
    boolean isIdentifier() {
        return isIdentifier;
    }

    /**
     * Returns a view of all characteristics added to the {@code AttributeType} to build.
     * The returned list is <cite>live</cite>: changes in this builder are reflected in that list and conversely.
     * However the returned list allows only {@linkplain List#remove(Object) remove} operations;
     * new characteristics can be added only by calls to one of the {@code set/addCharacteristic(…)} methods.
     *
     * @return a live list over the characteristics declared to this builder.
     *
     * @see #addCharacteristic(Class)
     * @see #addCharacteristic(AttributeType)
     * @see #setValidValues(Object...)
     * @see #setCRS(CoordinateReferenceSystem)
     */
    public List<CharacteristicTypeBuilder<?>> characteristics() {
        return new RemoveOnlyList<>(characteristics);
    }

    /**
     * Replaces the given characteristic by a new one. Exactly one instance of the old characteristic
     * shall exist (this is not verified).
     *
     * @see CharacteristicTypeBuilder#setValueClass(Class)
     */
    final void replace(final CharacteristicTypeBuilder<?> old, final CharacteristicTypeBuilder<?> n) {
        /*
         * We do not verify if lastIndexOf(old) >= 0 because
         * an element not found would be a bug in our algorithm.
         */
        characteristics.set(characteristics.lastIndexOf(old), n);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeTypeBuilder<V> setDefinition(final CharSequence definition) {
        super.setDefinition(definition);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeTypeBuilder<V> setDesignation(final CharSequence designation) {
        super.setDesignation(designation);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeTypeBuilder<V> setDescription(final CharSequence description) {
        super.setDescription(description);
        return this;
    }

    /**
     * Appends a text inside the value returned by {@link #toString()}, before the closing bracket.
     */
    @Override
    final void toStringInternal(final StringBuilder buffer) {
        buffer.append(" : ").append(Classes.getShortName(valueClass));
    }

    /**
     * Creates a new attribute type from the current setting.
     */
    @Override
    final PropertyType create() {
        final AttributeType<?>[] chrts = new AttributeType<?>[characteristics.size()];
        for (int i=0; i<chrts.length; i++) {
            chrts[i] = characteristics.get(i).build();
        }
        return new DefaultAttributeType<>(identification(), valueClass, minimumOccurs, maximumOccurs, defaultValue, chrts);
    }
}
