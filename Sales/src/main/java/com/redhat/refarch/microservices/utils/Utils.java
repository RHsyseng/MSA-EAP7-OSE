package com.redhat.refarch.microservices.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Utils
{

	private static final Map<Class<?>, PropertyDescriptor[]> beanDescriptors = new HashMap<Class<?>, PropertyDescriptor[]>();

	public static <T> void copy(T source, T destination, boolean skipIfNull)
	{
		PropertyDescriptor[] descriptors = getBeanDescriptors( source.getClass() );
		for( PropertyDescriptor descriptor : descriptors )
		{
			try
			{
				if( "class".equals( descriptor.getName() ) )
				{
					//Class is not a regular JavaBeans property!
					continue;
				}
				Method readMethod = descriptor.getReadMethod();
				Method writeMethod = descriptor.getWriteMethod();
				if( readMethod == null || writeMethod == null )
				{
					//Property must be read/write to copy
					continue;
				}
				Object value = readMethod.invoke( source );
				if( value == null && skipIfNull == true )
				{
					//As per the flag, do not copy null properties
					continue;
				}
				else
				{
					writeMethod.invoke( destination, value );
				}
			}
			catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e )
			{
				throw new IllegalStateException( e );
			}
		}
	}

	private static PropertyDescriptor[] getBeanDescriptors(Class<?> clazz)
	{
		PropertyDescriptor[] descriptors = beanDescriptors.get( clazz );
		if( descriptors == null )
		{
			try
			{
				BeanInfo beanInfo = Introspector.getBeanInfo( clazz );
				return beanInfo.getPropertyDescriptors();
			}
			catch( IntrospectionException e )
			{
				throw new IllegalStateException( e );
			}
		}
		return descriptors;
	}
}
