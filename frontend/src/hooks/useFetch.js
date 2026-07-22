import { useCallback, useEffect, useState } from 'react'
import { fetchData } from '../utils/fetchData'

export function useFetch(url, options = {}) {
  const { immediate = true, initialData = null } = options

  const [data, setData] = useState(initialData)
  const [loading, setLoading] = useState(Boolean(immediate && url))
  const [error, setError] = useState(null)

  const fetchDataFromApi = useCallback(async () => {
    if (!url) {
      return null
    }

    setLoading(true)
    setError(null)

    try {
      const result = await fetchData(url)
      setData(result)
      return result
    } catch (err) {
      setError(err.message)
      return null
    } finally {
      setLoading(false)
    }
  }, [url])

  useEffect(() => {
    if (!immediate || !url) {
      return undefined
    }

    let isActive = true

    const loadData = async () => {
      setLoading(true)
      setError(null)

      try {
        const result = await fetchData(url)
        if (isActive) {
          setData(result)
        }
      } catch (err) {
        if (isActive) {
          setError(err.message)
        }
      } finally {
        if (isActive) {
          setLoading(false)
        }
      }
    }

    loadData()

    return () => {
      isActive = false
    }
  }, [url, immediate])

  return {
    data,
    loading,
    error,
    refetch: fetchDataFromApi,
    setData,
  }
}
