require_relative '../../utils'
require_relative '../../logging'
require_relative '../../plugin_channel'
require_relative '../../core_emitter'
require_relative '../../core_processor'

module EVD::Plugin::Tunnel
  class BaseProtocol
    include EVD::Logging

    def initialize core, output, connection
      @core = core
      @output = output
      @connection = connection
      @metadata = nil
      @processor = nil
      @tunnel_id = nil
      @subs = {}
    end

    def send_data data
      @connection.send_data data
    end

    def set_text_mode size
      @connection.set_text_mode size
    end

    def tunnel_frame id, addr, data
      if s = @subs[id]
        s.call id, addr, data
      else
        log.error "Nothing listening on #{id}'"
      end
    end

    def parse_protocol string
      return Socket::SOCK_STREAM if string == :tcp
      return Socket::SOCK_DGRAM if string == :udp
      raise "Unsupported protocol: #{string}"
    end

    def subscribe protocol, port, &block
      protocol = parse_protocol protocol
      id = [protocol, port]

      if @subs[id]
        raise "Only one plugin at a time can tunnel port '#{port}'"
      end

      @subs[id] = block
    end

    def stop
      @processor.stop if @processor
      @core.debug.unmonitor @tunnel_id if @tunnel_id
      @core.statistics.unregister @tunnel_id if @tunnel_id
      @metadata = nil
      @processor = nil
      @tunnel_id = nil
      @subs = {}
    end

    def read_metadata data
      d = {}

      d[:tags] = EVD.merge_sets @core.tags, data["tags"]
      d[:attributes] = EVD.merge_sets @core.attributes, data["attributes"]

      if host = data["host"]
        d[:host] = host
      end

      if ttl = data["ttl"]
        d[:ttl] = ttl
      end

      d
    end

    def receive_metadata data
      @metadata = read_metadata data

      input = EVD::PluginChannel.new 'tunnel'

      @core.tunnels.each do |t|
        t.start input, @output, self
      end

      response = {:type => self.class.type}

      response[:bind] = @subs.keys.map do |protocol, port|
        {:protocol => protocol, :port => port}
      end

      response = JSON.dump(response)

      send_data "#{response}\n"

      # setup a small core
      emitter = EVD::CoreEmitter.new @output, @metadata
      @processor = EVD::CoreProcessor.new emitter, @core.processors
      @processor.start input

      @reporter = EVD::CoreReporter.new @processor.reporters

      if host = @metadata[:host]
        @tunnel_id = "tunnel.input/#{host}"
      else
        @tunnel_id = "tunnel.input/#{@connection.get_peer}"
      end

      @core.debug.monitor @tunnel_id, input, EVD::Debug::Input
      @core.statistics.register @tunnel_id, @reporter
    end

  end
end