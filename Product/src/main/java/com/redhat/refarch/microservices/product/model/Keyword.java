package com.redhat.refarch.microservices.product.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;

@Entity
@NamedQuery(name = "Keyword.findKeyword", query = "SELECT k FROM Keyword k WHERE UPPER(k.keyword) = UPPER(:query)")
public class Keyword
{

	@Id
	private String keyword;

	@ManyToMany(fetch = FetchType.EAGER, mappedBy = "keywords")
	private List<Product> products;

	public String getKeyword()
	{
		return keyword;
	}

	public void setKeyword(String keyword)
	{
		this.keyword = keyword;
	}

	public List<Product> getProducts()
	{
		return products;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( keyword == null ) ? 0 : keyword.hashCode() );
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
			return true;
		if( obj == null )
			return false;
		if( getClass() != obj.getClass() )
			return false;
		Keyword other = (Keyword)obj;
		if( keyword == null )
		{
			if( other.keyword != null )
				return false;
		}
		else if( !keyword.equals( other.keyword ) )
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "Keyword [keyword=" + keyword + "]";
	}
}