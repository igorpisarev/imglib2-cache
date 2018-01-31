package net.imglib2.cache.img.list;

import java.util.Iterator;

import net.imglib2.AbstractCursor;
import net.imglib2.AbstractInterval;
import net.imglib2.AbstractLocalizable;
import net.imglib2.AbstractLocalizingCursor;
import net.imglib2.Cursor;
import net.imglib2.Dirty;
import net.imglib2.FlatIterationOrder;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.list.ListImg;
import net.imglib2.img.list.access.container.AbstractList;
import net.imglib2.img.list.cell.AbstractCellListImg;
import net.imglib2.util.IntervalIndexer;

/**
 * A {@link ListImg} representing a single cell of an {@link AbstractCellListImg}.
 * It is similar to {@link ListImg} except that minimum is not at the origin
 * but at the minimum of the cell within the {@link AbstractCellListImg}.
 *
 * @param <T>
 *            pixel type
 *
 * @author Tobias Pietzsch
 * @author Igor Pisarev
 */
public class SingleCellListImg< T, A extends AbstractList< T, A > >
	extends AbstractInterval
	implements Img< T >
{
	private final int[] steps;

	private final int[] dimensions;

	private long size;

	private A data;

	private Dirty dirty;

	public SingleCellListImg( final int[] cellDims, final long[] cellMin, final A cellData, final Dirty dirtyFlag )
	{
		this( cellDims.length );
		reset( cellDims, cellMin, cellData, dirtyFlag );
	}

	private SingleCellListImg( final int n )
	{
		super( n );
		steps = new int[ n ];
		dimensions = new int[ n ];
	}

	/**
	 * If the cell is backed by a {@link Dirty}-capable access, flag it as
	 * dirty. (Otherwise, do nothing.)
	 */
	public void setDirty()
	{
		dirty.setDirty();
	}

	public A getData()
	{
		return data;
	}

	void reset(	final int[] cellDims, final long[] cellMin, final A cellData, final Dirty dirtyFlag )
	{
		for ( int d = 0; d < n; ++d )
		{
			min[ d ] = cellMin[ d ];
			max[ d ] = min[ d ] + cellDims[ d ] - 1;
			dimensions[ d ] = cellDims[ d ];
		}
		IntervalIndexer.createAllocationSteps( cellDims, steps );
		size = steps[ n - 1 ] * cellDims[ n - 1 ];
		data = cellData;
		dirty = dirtyFlag;
	}

	@Override
	public RandomAccess< T > randomAccess()
	{
		return new CellListRandomAccess();
	}

	@Override
	public RandomAccess< T > randomAccess( final Interval interval )
	{
		return randomAccess();
	}

	class CellListRandomAccess extends AbstractLocalizable implements RandomAccess< T >
	{
		int index;

		CellListRandomAccess( final CellListRandomAccess randomAccess )
		{
			super( SingleCellListImg.this.n );
			index = randomAccess.index;
			System.arraycopy( randomAccess.position, 0, position, 0, n );
		}

		CellListRandomAccess()
		{
			super( SingleCellListImg.this.n );
			index = 0;
			System.arraycopy( min, 0, position, 0, n );
		}

		@Override
		public T get()
		{
			return data.getValue( index );
		}

		@Override
		public void fwd( final int d )
		{
			index += steps[ d ];
			++position[ d ];
		}

		@Override
		public void bck( final int d )
		{
			index -= steps[ d ];
			--position[ d ];
		}

		@Override
		public void move( final int distance, final int d )
		{
			index += steps[ d ] * distance;
			position[ d ] += distance;
		}

		@Override
		public void move( final long distance, final int d )
		{
			index += steps[ d ] * ( int ) distance;
			position[ d ] += distance;
		}

		@Override
		public void move( final Localizable localizable )
		{
			index = 0;
			for ( int d = 0; d < n; ++d )
			{
				final int distance = localizable.getIntPosition( d );
				position[ d ] += distance;
				index += distance * steps[ d ];
			}
		}

		@Override
		public void move( final int[] distance )
		{
			index = 0;
			for ( int d = 0; d < n; ++d )
			{
				position[ d ] += distance[ d ];
				index += distance[ d ] * steps[ d ];
			}
		}

		@Override
		public void move( final long[] distance )
		{
			index = 0;
			for ( int d = 0; d < n; ++d )
			{
				position[ d ] += distance[ d ];
				index += ( int ) distance[ d ] * steps[ d ];
			}
		}

		@Override
		public void setPosition( final Localizable localizable )
		{
			localizable.localize( position );
			index = 0;
			for ( int d = 0; d < n; ++d )
				index += ( int ) ( position[ d ] - min[ d ] ) * steps[ d ];
		}

		@Override
		public void setPosition( final int[] pos )
		{
			index = 0;
			for ( int d = 0; d < n; ++d )
			{
				position[ d ] = pos[ d ];
				index += ( int ) ( pos[ d ] - min[ d ] ) * steps[ d ];
			}
		}

		@Override
		public void setPosition( final long[] pos )
		{
			index = 0;
			for ( int d = 0; d < n; ++d )
			{
				position[ d ] = pos[ d ];
				index += ( int ) ( pos[ d ] - min[ d ] ) * steps[ d ];
			}
		}

		@Override
		public void setPosition( final int pos, final int d )
		{
			index += ( pos - position[ d ] ) * steps[ d ];
			position[ d ] = pos;
		}

		@Override
		public void setPosition( final long pos, final int d )
		{
			index += ( int ) ( pos - position[ d ] ) * steps[ d ];
			position[ d ] = pos;
		}

		@Override
		public CellListRandomAccess copy()
		{
			return new CellListRandomAccess( this );
		}

		@Override
		public CellListRandomAccess copyRandomAccess()
		{
			return copy();
		}
	}

	@Override
	public Cursor< T > cursor()
	{
		return new CellListCursor();
	}

	class CellListCursor extends AbstractCursor< T >
	{
		int index;
		final int lastIndex;

		CellListCursor()
		{
			super( SingleCellListImg.this.n );
			lastIndex = ( int ) SingleCellListImg.this.size() - 1;
			reset();
		}

		CellListCursor( final CellListCursor other )
		{
			super( SingleCellListImg.this.n );
			index = other.index;
			lastIndex = other.lastIndex;
		}

		@Override
		public T get()
		{
			return data.getValue( index );
		}

		@Override
		public boolean hasNext()
		{
			return index < lastIndex;
		}

		@Override
		public void jumpFwd( final long steps )
		{
			index += ( int ) steps;
		}

		@Override
		public T next()
		{
			fwd();
			return get();
		}

		@Override
		public void fwd()
		{
			++index;
		}

		@Override
		public void reset()
		{
			index = -1;
		}

		@Override
		public void localize( final long[] position )
		{
			IntervalIndexer.indexToPositionWithOffset( index, dimensions, min, position );
		}

		@Override
		public long getLongPosition( final int d )
		{
			return IntervalIndexer.indexToPositionWithOffset( index, dimensions, steps, min, d );
		}

		@Override
		public CellListCursor copy()
		{
			return new CellListCursor( this );
		}

		@Override
		public CellListCursor copyCursor()
		{
			return copy();
		}
	}

	@Override
	public Cursor< T > localizingCursor()
	{
		return new CellListLocalizingCursor();
	}

	class CellListLocalizingCursor extends AbstractLocalizingCursor< T >
	{
		int index;

		final int lastIndex;

		CellListLocalizingCursor()
		{
			super( SingleCellListImg.this.n );
			lastIndex = ( int ) SingleCellListImg.this.size() - 1;
			reset();
		}

		CellListLocalizingCursor( final CellListLocalizingCursor other )
		{
			super( other.n );
			index = other.index;
			lastIndex = other.lastIndex;
			System.arraycopy( other.position, 0, position, 0, n );
		}

		@Override
		public T get()
		{
			return data.getValue( index );
		}

		@Override
		public void fwd()
		{
			++index;
			for ( int d = 0; d < n && ++position[ d ] > max[ d ]; ++d )
				position[ d ] = min[ d ];
		}

		@Override
		public void jumpFwd( final long steps )
		{
			index += ( int ) steps;
			IntervalIndexer.indexToPositionWithOffset( index, dimensions, min, position );
		}

		@Override
		public T next()
		{
			fwd();
			return get();
		}

		@Override
		public boolean hasNext()
		{
			return index < lastIndex;
		}

		@Override
		public void reset()
		{
			index = -1;
			System.arraycopy( min, 0, position, 0, n );
			position[ 0 ]--;
		}

		@Override
		public CellListLocalizingCursor copy()
		{
			return new CellListLocalizingCursor( this );
		}

		@Override
		public CellListLocalizingCursor copyCursor()
		{
			return copy();
		}
	}

	@Override
	public ImgFactory< T > factory()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Img< T > copy()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long size()
	{
		return size;
	}

	@Override
	public T firstElement()
	{
		return cursor().next();
	}

	@Override
	public Object iterationOrder()
	{
		return new FlatIterationOrder( this );
	}

	@Override
	public Iterator< T > iterator()
	{
		return cursor();
	}
}
