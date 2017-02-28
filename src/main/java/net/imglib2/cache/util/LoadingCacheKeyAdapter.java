package net.imglib2.cache.util;

import java.util.concurrent.ExecutionException;

import net.imglib2.cache.LoadingCache;

public class LoadingCacheKeyAdapter< K, L, V, C extends LoadingCache< L, V > >
		extends AbstractCacheKeyAdapter< K, L, V, C >
		implements LoadingCache< K, V >
{
	public LoadingCacheKeyAdapter( final C cache, final KeyBimap< K, L > keymap )
	{
		super( cache, keymap );
	}

	@Override
	public V get( final K key ) throws ExecutionException
	{
		return cache.get( keymap.getTarget( key ) );
	}
}