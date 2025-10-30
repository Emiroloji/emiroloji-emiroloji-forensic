/** @type {import('next').NextConfig} */
const nextConfig = {
  // experimental: {
  //   taint: true,
  // },
  typescript: {
    ignoreBuildErrors: true, // <-- BU SATIRI VE ÇEVRELEYEN { } BLOĞUNU YORUMDAN ÇIKARIN
  },
}

export default nextConfig