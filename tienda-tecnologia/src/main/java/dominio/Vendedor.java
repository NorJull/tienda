package dominio;

import dominio.repositorio.RepositorioProducto;
import persistencia.builder.ProductoBuilder;

import java.util.Calendar;
import java.util.Date;

import dominio.excepcion.GarantiaExtendidaException;
import dominio.repositorio.RepositorioGarantiaExtendida;

public class Vendedor {

	public static final String EL_PRODUCTO_TIENE_GARANTIA = "El producto ya cuenta con una garantia extendida";
	public static final String EL_CODIGO_PRODUCTO_TIENE_TRES_VOCALES = "Este producto no cuenta con garantía extendida";

	private RepositorioProducto repositorioProducto;
	private RepositorioGarantiaExtendida repositorioGarantia;

	public Vendedor(RepositorioProducto repositorioProducto, RepositorioGarantiaExtendida repositorioGarantia) {
		this.repositorioProducto = repositorioProducto;
		this.repositorioGarantia = repositorioGarantia;

	}

	public void generarGarantia(String codigo, String nombreCliente) {

		// Si el producto tiene garantía se lanza una excepción
		if (tieneGarantia(codigo))
			throw new GarantiaExtendidaException(EL_PRODUCTO_TIENE_GARANTIA);

		// Si el código del producto tiene 3 vocales se lanza una excepción
		String upperCodigo = codigo.toUpperCase();
		String vocales = "AEIOU";
		int contadorVocales = 0;

		for (int i = 0; i < upperCodigo.length(); i++) {

			for (int j = 0; j < vocales.length(); j++) {

				if (upperCodigo.charAt(i) == vocales.charAt(j))
					contadorVocales++;
			}
		}

		if (contadorVocales == 3)
			throw new GarantiaExtendidaException(EL_CODIGO_PRODUCTO_TIENE_TRES_VOCALES);

		Producto producto = repositorioProducto.obtenerPorCodigo(codigo);

		// Crear garantía para el producto y con la fecha actual
		GarantiaExtendida garantiaExtendida = new GarantiaExtendida(producto);

		// Fijar nombre del cliente
		garantiaExtendida.setNombreCliente(nombreCliente);

		// Fijar el precio de la garantia
		garantiaExtendida.setPrecioGarantia(calcularPrecioGarantia(producto.getPrecio()));

		// Fijar la fecha en la que finaliza la garantía extendida.
		garantiaExtendida.setFechaFinGarantia(
				calcularFechaFinGarantia(producto.getPrecio(), garantiaExtendida.getFechaSolicitudGarantia()));

		repositorioGarantia.agregar(garantiaExtendida);

	}

	public boolean tieneGarantia(String codigo) {

		Producto producto = repositorioGarantia.obtenerProductoConGarantiaPorCodigo(codigo);

		return producto != null ? true : false;

	}

	public double calcularPrecioGarantia(double precioProducto) {

		return precioProducto > 500000 ? precioProducto * 0.2 : precioProducto * 0.1;
	}

	public Date calcularFechaFinGarantia(double precioProducto, Date fechaSolicitudGarantia) {

		Date fechaFinGarantia = null;

		Calendar calFechaFinGarantia = Calendar.getInstance();
		calFechaFinGarantia.setTime(fechaSolicitudGarantia);

		if (precioProducto > 500000) {
			// 200 días incluyendo el día de la solicitud
			calFechaFinGarantia.add(Calendar.DAY_OF_YEAR, 199);

			Calendar calFechaSolicitud = Calendar.getInstance();
			calFechaSolicitud.setTime(fechaSolicitudGarantia);

			int contadorLunes = 0;

			// Contar número de lunes entre las dos fechas
			while (calFechaFinGarantia.after(calFechaSolicitud)) {

				if (calFechaSolicitud.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
					contadorLunes++;

				calFechaSolicitud.add(Calendar.DAY_OF_YEAR, 1);
			}

			// Aumentar la cantidad de lunes entre las fecha (Fecha solicitud - Fecha final
			// de garantía)
			calFechaFinGarantia.add(Calendar.DAY_OF_YEAR, contadorLunes);

			// Si la fecha en la que finaliza la garantía extendida cae un
			// domingo deberá finalizar el siguiente día hábil
			if (calFechaFinGarantia.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				calFechaFinGarantia.add(Calendar.DAY_OF_YEAR, 1);
			}
			// Obtener la fecha
			fechaFinGarantia = calFechaFinGarantia.getTime();

		} else {

			// 100 días incluyendo el día de la solicitud
			calFechaFinGarantia.add(Calendar.DAY_OF_YEAR, 99);
			fechaFinGarantia = calFechaFinGarantia.getTime();

		}

		return fechaFinGarantia;
	}

}
